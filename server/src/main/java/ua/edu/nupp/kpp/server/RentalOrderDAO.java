package ua.edu.nupp.kpp.server;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблиці rental_order. */
public class RentalOrderDAO {
    private static final String COLUMNS =
            "id, client_id, equipment_id, start_date, end_date, state, f1, f2, f3, priority_index";

    public List<RentalOrder> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM rental_order ORDER BY id";
        List<RentalOrder> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public RentalOrder getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM rental_order WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public RentalOrder add(RentalOrder o) throws SQLException {
        String sql = "INSERT INTO rental_order(client_id, equipment_id, start_date, end_date, state, "
                + "f1, f2, f3, priority_index) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, o.getClientId());
            p.setLong(2, o.getEquipmentId());
            p.setDate(3, Date.valueOf(o.getStartDate()));
            p.setDate(4, Date.valueOf(o.getEndDate()));
            p.setString(5, o.getState().name());
            p.setBigDecimal(6, BigDecimal.valueOf(o.getF1()));
            p.setBigDecimal(7, BigDecimal.valueOf(o.getF2()));
            p.setBigDecimal(8, BigDecimal.valueOf(o.getF3()));
            p.setBigDecimal(9, BigDecimal.valueOf(o.getPriorityIndex()));
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    o.setId(keys.getLong(1));
                }
            }
        }
        return o;
    }

    /** Оновлює дані заявки, але не стан (стан змінюється лише через updateState). */
    public boolean update(RentalOrder o) throws SQLException {
        String sql = "UPDATE rental_order SET client_id = ?, equipment_id = ?, start_date = ?, end_date = ?, "
                + "f1 = ?, f2 = ?, f3 = ?, priority_index = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, o.getClientId());
            p.setLong(2, o.getEquipmentId());
            p.setDate(3, Date.valueOf(o.getStartDate()));
            p.setDate(4, Date.valueOf(o.getEndDate()));
            p.setBigDecimal(5, BigDecimal.valueOf(o.getF1()));
            p.setBigDecimal(6, BigDecimal.valueOf(o.getF2()));
            p.setBigDecimal(7, BigDecimal.valueOf(o.getF3()));
            p.setBigDecimal(8, BigDecimal.valueOf(o.getPriorityIndex()));
            p.setLong(9, o.getId());
            return p.executeUpdate() > 0;
        }
    }

    public boolean updateState(long id, OrderState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE rental_order SET state = ? WHERE id = ?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            return p.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM rental_order WHERE id = ?")) {
            p.setLong(1, id);
            return p.executeUpdate() > 0;
        }
    }

    private RentalOrder map(ResultSet rs) throws SQLException {
        RentalOrder o = new RentalOrder();
        o.setId(rs.getLong("id"));
        o.setClientId(rs.getLong("client_id"));
        o.setEquipmentId(rs.getLong("equipment_id"));
        o.setStartDate(rs.getDate("start_date").toLocalDate());
        o.setEndDate(rs.getDate("end_date").toLocalDate());
        o.setState(OrderState.valueOf(rs.getString("state")));
        o.setF1(rs.getDouble("f1"));
        o.setF2(rs.getDouble("f2"));
        o.setF3(rs.getDouble("f3"));
        o.setPriorityIndex(rs.getDouble("priority_index"));
        return o;
    }
}
