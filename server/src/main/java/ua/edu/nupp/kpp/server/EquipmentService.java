package ua.edu.nupp.kpp.server;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class EquipmentService {
    private static final Logger LOG = Logger.getLogger(EquipmentService.class.getName());
    private final EquipmentDAO dao = new EquipmentDAO();

    public List<Equipment> getAll() throws SQLException {
        return dao.getAll();
    }

    public Equipment add(Equipment e) throws SQLException {
        validate(e);
        if (dao.findByCode(e.getCode()) != null) {
            throw new IllegalArgumentException("Обладнання з кодом " + e.getCode() + " уже існує");
        }
        dao.add(e);
        LOG.info("Додано обладнання: " + e.getCode());
        return e;
    }

    public Equipment update(Equipment e) throws SQLException {
        if (e.getId() == null) {
            throw new IllegalArgumentException("Вкажіть ID запису для редагування");
        }
        validate(e);
        if (dao.getById(e.getId()) == null) {
            throw new IllegalArgumentException("Обладнання з ID " + e.getId() + " не знайдено");
        }
        Equipment same = dao.findByCode(e.getCode());
        if (same != null && !same.getId().equals(e.getId())) {
            throw new IllegalArgumentException("Обладнання з кодом " + e.getCode() + " уже існує");
        }
        dao.update(e);
        LOG.info("Оновлено обладнання: " + e.getId());
        return e;
    }

    public void delete(long id) throws SQLException {
        if (!dao.delete(id)) {
            throw new IllegalArgumentException("Обладнання з ID " + id + " не знайдено");
        }
        LOG.info("Видалено обладнання: " + id);
    }

    /** Предметна валідація (не потребує бази даних). */
    public static void validate(Equipment e) {
        if (isBlank(e.getCode())) {
            throw new IllegalArgumentException("Код обладнання не може бути порожнім");
        }
        if (isBlank(e.getName())) {
            throw new IllegalArgumentException("Назва обладнання не може бути порожньою");
        }
        if (isBlank(e.getCategory())) {
            throw new IllegalArgumentException("Категорія обладнання не може бути порожньою");
        }
        if (e.getPricePerDay() == null || e.getPricePerDay().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ціна за день має бути більшою за 0");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
