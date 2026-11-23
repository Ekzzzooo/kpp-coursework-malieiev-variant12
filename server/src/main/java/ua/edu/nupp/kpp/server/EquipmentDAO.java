package ua.edu.nupp.kpp.server;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблиці equipment. Усі SQL-запити виконуються через PreparedStatement. */
public class EquipmentDAO {
    private static final String COLUMNS = "id, code, name, category, price_per_day";

    public List<Equipment> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM equipment ORDER BY id";
        List<Equipment> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Equipment getById(long id) throws SQLException {
        return findOne("SELECT " + COLUMNS + " FROM equipment WHERE id = ?", id);
    }

    public Equipment findByCode(String code) throws SQLException {
        return findOne("SELECT " + COLUMNS + " FROM equipment WHERE code = ?", code);
    }

    public Equipment add(Equipment e) throws SQLException {
        String sql = "INSERT INTO equipment(code, name, category, price_per_day) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, e.getCode());
            p.setString(2, e.getName());
            p.setString(3, e.getCategory());
            p.setBigDecimal(4, e.getPricePerDay());
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getLong(1));
                }
            }
        }
        return e;
    }

    public boolean update(Equipment e) throws SQLException {
        String sql = "UPDATE equipment SET code = ?, name = ?, category = ?, price_per_day = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, e.getCode());
            p.setString(2, e.getName());
            p.setString(3, e.getCategory());
            p.setBigDecimal(4, e.getPricePerDay());
            p.setLong(5, e.getId());
            return p.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM equipment WHERE id = ?")) {
            p.setLong(1, id);
            return p.executeUpdate() > 0;
        }
    }

    private Equipment findOne(String sql, Object param) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, param);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Equipment map(ResultSet rs) throws SQLException {
        return new Equipment(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                rs.getString("category"), rs.getBigDecimal("price_per_day"));
    }
}
