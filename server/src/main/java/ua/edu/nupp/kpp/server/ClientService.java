package ua.edu.nupp.kpp.server;


import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ClientService {
    private static final Logger LOG = Logger.getLogger(ClientService.class.getName());
    private final ClientDAO dao = new ClientDAO();

    public List<Client> getAll() throws SQLException {
        return dao.getAll();
    }

    public Client add(Client c) throws SQLException {
        validate(c);
        dao.add(c);
        LOG.info("Додано клієнта: " + c.getFullName());
        return c;
    }

    public Client update(Client c) throws SQLException {
        if (c.getId() == null) {
            throw new IllegalArgumentException("Вкажіть ID запису для редагування");
        }
        validate(c);
        if (!dao.update(c)) {
            throw new IllegalArgumentException("Клієнта з ID " + c.getId() + " не знайдено");
        }
        LOG.info("Оновлено клієнта: " + c.getId());
        return c;
    }

    public void delete(long id) throws SQLException {
        if (!dao.delete(id)) {
            throw new IllegalArgumentException("Клієнта з ID " + id + " не знайдено");
        }
        LOG.info("Видалено клієнта: " + id);
    }

    public static void validate(Client c) {
        if (c.getFullName() == null || c.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("ПІБ клієнта не може бути порожнім");
        }
        if (c.getPhone() == null || c.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Телефон клієнта не може бути порожнім");
        }
        if (c.getEmail() != null && !c.getEmail().trim().isEmpty() && !c.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email має містити символ @");
        }
    }
}
