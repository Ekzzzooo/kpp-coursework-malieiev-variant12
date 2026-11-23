package ua.edu.nupp.kpp.server;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка заявок: валідація, розрахунок F1 та I, життєвий цикл. */
public class RentalOrderService {
    private static final Logger LOG = Logger.getLogger(RentalOrderService.class.getName());
    private final RentalOrderDAO dao = new RentalOrderDAO();
    private final ClientDAO clientDao = new ClientDAO();
    private final EquipmentDAO equipmentDao = new EquipmentDAO();
    private final RentalContractDAO contractDao = new RentalContractDAO();

    public List<RentalOrder> getAll() throws SQLException {
        return dao.getAll();
    }

    public RentalOrder add(RentalOrder o) throws SQLException {
        validate(o);
        requireReferences(o);
        o.setState(OrderState.CREATED);
        calculate(o, LocalDate.now());
        dao.add(o);
        LOG.info("Створено заявку " + o.getId() + ", I = " + o.getPriorityIndex());
        return o;
    }

    public RentalOrder update(RentalOrder o) throws SQLException {
        if (o.getId() == null) {
            throw new IllegalArgumentException("Вкажіть ID запису для редагування");
        }
        validate(o);
        RentalOrder existing = dao.getById(o.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Заявку з ID " + o.getId() + " не знайдено");
        }
        if (existing.getState() == OrderState.ISSUED || existing.getState() == OrderState.RETURNED) {
            throw new IllegalArgumentException("Заявку у стані " + existing.getState() + " редагувати не можна");
        }
        requireReferences(o);
        o.setState(existing.getState());
        calculate(o, LocalDate.now());
        dao.update(o);
        LOG.info("Оновлено заявку " + o.getId());
        return o;
    }

    /** Зміна стану життєвого циклу: рішення про допустимість приймає сервер. */
    public RentalOrder changeState(long id, OrderState to) throws SQLException {
        RentalOrder o = dao.getById(id);
        if (o == null) {
            throw new IllegalArgumentException("Заявку з ID " + id + " не знайдено");
        }
        if (!isAllowedTransition(o.getState(), to)) {
            throw new IllegalArgumentException("Перехід " + o.getState() + " → " + to + " недопустимий");
        }
        if (to == OrderState.ISSUED && contractDao.findByOrderId(id) == null) {
            throw new IllegalArgumentException("Для видачі обладнання спочатку оформіть договір прокату");
        }
        dao.updateState(id, to);
        o.setState(to);
        LOG.info("Змінено стан заявки " + id + " на " + to);
        return o;
    }

    public void delete(long id) throws SQLException {
        if (contractDao.findByOrderId(id) != null) {
            throw new IllegalArgumentException("Спочатку видаліть договір прокату цієї заявки");
        }
        if (!dao.delete(id)) {
            throw new IllegalArgumentException("Заявку з ID " + id + " не знайдено");
        }
        LOG.info("Видалено заявку " + id);
    }

    /** Дозволені лише послідовні переходи CREATED → CONFIRMED → ISSUED → RETURNED. */
    public static boolean isAllowedTransition(OrderState from, OrderState to) {
        return (from == OrderState.CREATED && to == OrderState.CONFIRMED)
                || (from == OrderState.CONFIRMED && to == OrderState.ISSUED)
                || (from == OrderState.ISSUED && to == OrderState.RETURNED);
    }

    public static void validate(RentalOrder o) {
        if (o.getClientId() <= 0) {
            throw new IllegalArgumentException("Вкажіть ID клієнта");
        }
        if (o.getEquipmentId() <= 0) {
            throw new IllegalArgumentException("Вкажіть ID обладнання");
        }
        if (o.getStartDate() == null || o.getEndDate() == null) {
            throw new IllegalArgumentException("Вкажіть дати початку та завершення прокату");
        }
        if (o.getEndDate().isBefore(o.getStartDate())) {
            throw new IllegalArgumentException("Дата завершення не може бути раніше дати початку");
        }
        if (o.getF2() < 0 || o.getF2() > 100 || o.getF3() < 0 || o.getF3() > 100) {
            throw new IllegalArgumentException("F2 і F3 мають бути в діапазоні 0..100");
        }
    }

    private void requireReferences(RentalOrder o) throws SQLException {
        if (clientDao.getById(o.getClientId()) == null) {
            throw new IllegalArgumentException("Клієнта з ID " + o.getClientId() + " не знайдено");
        }
        if (equipmentDao.getById(o.getEquipmentId()) == null) {
            throw new IllegalArgumentException("Обладнання з ID " + o.getEquipmentId() + " не знайдено");
        }
    }

    /** F1 та I розраховуються на сервері, користувач їх не вводить. */
    static void calculate(RentalOrder o, LocalDate today) {
        o.setF1(PriorityCalculator.factor1(today, o.getStartDate()));
        o.setPriorityIndex(PriorityCalculator.calculate(o.getF1(), o.getF2(), o.getF3()));
    }
}
