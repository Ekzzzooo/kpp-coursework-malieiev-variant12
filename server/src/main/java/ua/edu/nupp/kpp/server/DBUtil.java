package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Підключення до MySQL. Параметри можна перевизначити змінними середовища
 * DB_URL, DB_USER, DB_PASSWORD, тому пароль не потрапляє до репозиторію.
 * Значення за замовчуванням відповідають типовому XAMPP.
 */
public final class DBUtil {
    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/equipment_rental?useUnicode=true&characterEncoding=UTF-8");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
