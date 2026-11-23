package ua.edu.nupp.kpp.server;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблиці client. */
public class ClientDAO {
    private static final String COLUMNS = "id, full_name, phone, email, address";

    public List<Client> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM client ORDER BY id";
        List<Client> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Client getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM client WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Client add(Client cl) throws SQLException {
        String sql = "INSERT INTO client(full_name, phone, email, address) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, cl.getFullName());
            p.setString(2, cl.getPhone());
            p.setString(3, cl.getEmail());
            p.setString(4, cl.getAddress());
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    cl.setId(keys.getLong(1));
                }
            }
        }
        return cl;
    }

    public boolean update(Client cl) throws SQLException {
        String sql = "UPDATE client SET full_name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, cl.getFullName());
            p.setString(2, cl.getPhone());
            p.setString(3, cl.getEmail());
            p.setString(4, cl.getAddress());
            p.setLong(5, cl.getId());
            return p.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM client WHERE id = ?")) {
            p.setLong(1, id);
            return p.executeUpdate() > 0;
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getLong("id"), rs.getString("full_name"), rs.getString("phone"),
                rs.getString("email"), rs.getString("address"));
    }
}
