package ua.edu.nupp.kpp.server;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблиці rental_contract. */
public class RentalContractDAO {
    private static final String COLUMNS = "id, order_id, contract_number, sign_date, total_price";

    public List<RentalContract> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM rental_contract ORDER BY id";
        List<RentalContract> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public RentalContract getById(long id) throws SQLException {
        return findOne("SELECT " + COLUMNS + " FROM rental_contract WHERE id = ?", id);
    }

    public RentalContract findByOrderId(long orderId) throws SQLException {
        return findOne("SELECT " + COLUMNS + " FROM rental_contract WHERE order_id = ?", orderId);
    }

    public RentalContract findByNumber(String number) throws SQLException {
        return findOne("SELECT " + COLUMNS + " FROM rental_contract WHERE contract_number = ?", number);
    }

    public RentalContract add(RentalContract k) throws SQLException {
        String sql = "INSERT INTO rental_contract(order_id, contract_number, sign_date, total_price) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, k.getOrderId());
            p.setString(2, k.getContractNumber());
            p.setDate(3, Date.valueOf(k.getSignDate()));
            p.setBigDecimal(4, k.getTotalPrice());
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    k.setId(keys.getLong(1));
                }
            }
        }
        return k;
    }

    public boolean update(RentalContract k) throws SQLException {
        String sql = "UPDATE rental_contract SET contract_number = ?, sign_date = ?, total_price = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k.getContractNumber());
            p.setDate(2, Date.valueOf(k.getSignDate()));
            p.setBigDecimal(3, k.getTotalPrice());
            p.setLong(4, k.getId());
            return p.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM rental_contract WHERE id = ?")) {
            p.setLong(1, id);
            return p.executeUpdate() > 0;
        }
    }

    private RentalContract findOne(String sql, Object param) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, param);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private RentalContract map(ResultSet rs) throws SQLException {
        RentalContract k = new RentalContract();
        k.setId(rs.getLong("id"));
        k.setOrderId(rs.getLong("order_id"));
        k.setContractNumber(rs.getString("contract_number"));
        k.setSignDate(rs.getDate("sign_date").toLocalDate());
        k.setTotalPrice(rs.getBigDecimal("total_price"));
        return k;
    }
}
