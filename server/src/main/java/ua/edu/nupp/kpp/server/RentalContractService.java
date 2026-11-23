package ua.edu.nupp.kpp.server;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class RentalContractService {
    private static final Logger LOG = Logger.getLogger(RentalContractService.class.getName());
    private final RentalContractDAO dao = new RentalContractDAO();
    private final RentalOrderDAO orderDao = new RentalOrderDAO();
    private final EquipmentDAO equipmentDao = new EquipmentDAO();

    public List<RentalContract> getAll() throws SQLException {
        return dao.getAll();
    }

    public RentalContract add(RentalContract k) throws SQLException {
        validate(k);
        RentalOrder order = orderDao.getById(k.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("Заявку з ID " + k.getOrderId() + " не знайдено");
        }
        if (order.getState() == OrderState.CREATED) {
            throw new IllegalArgumentException("Договір оформлюється лише для підтвердженої заявки");
        }
        if (dao.findByOrderId(k.getOrderId()) != null) {
            throw new IllegalArgumentException("Для цієї заявки договір уже оформлено");
        }
        if (dao.findByNumber(k.getContractNumber()) != null) {
            throw new IllegalArgumentException("Договір з номером " + k.getContractNumber() + " уже існує");
        }
        k.setTotalPrice(priceFor(order));
        dao.add(k);
        LOG.info("Оформлено договір " + k.getContractNumber());
        return k;
    }

    public RentalContract update(RentalContract k) throws SQLException {
        if (k.getId() == null) {
            throw new IllegalArgumentException("Вкажіть ID запису для редагування");
        }
        validate(k);
        RentalContract existing = dao.getById(k.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Договір з ID " + k.getId() + " не знайдено");
        }
        RentalContract same = dao.findByNumber(k.getContractNumber());
        if (same != null && !same.getId().equals(k.getId())) {
            throw new IllegalArgumentException("Договір з номером " + k.getContractNumber() + " уже існує");
        }
        RentalOrder order = orderDao.getById(existing.getOrderId());
        k.setOrderId(existing.getOrderId());
        k.setTotalPrice(priceFor(order));
        dao.update(k);
        LOG.info("Оновлено договір " + k.getId());
        return k;
    }

    public void delete(long id) throws SQLException {
        RentalContract k = dao.getById(id);
        if (k == null) {
            throw new IllegalArgumentException("Договір з ID " + id + " не знайдено");
        }
        RentalOrder order = orderDao.getById(k.getOrderId());
        if (order != null && (order.getState() == OrderState.ISSUED || order.getState() == OrderState.RETURNED)) {
            throw new IllegalArgumentException("Договір не можна видалити, коли обладнання вже видано або повернуто");
        }
        dao.delete(id);
        LOG.info("Видалено договір " + id);
    }

    public static void validate(RentalContract k) {
        if (k.getOrderId() <= 0) {
            throw new IllegalArgumentException("Вкажіть ID заявки");
        }
        if (k.getContractNumber() == null || k.getContractNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Номер договору не може бути порожнім");
        }
        if (k.getSignDate() == null) {
            throw new IllegalArgumentException("Вкажіть дату підписання договору");
        }
    }

    /** Вартість = кількість днів прокату (включно) * ціна за день. */
    public static BigDecimal calculateTotal(LocalDate start, LocalDate end, BigDecimal pricePerDay) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        return pricePerDay.multiply(BigDecimal.valueOf(days)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal priceFor(RentalOrder order) throws SQLException {
        Equipment eq = equipmentDao.getById(order.getEquipmentId());
        if (eq == null) {
            throw new IllegalArgumentException("Обладнання заявки не знайдено");
        }
        return calculateTotal(order.getStartDate(), order.getEndDate(), eq.getPricePerDay());
    }
}
