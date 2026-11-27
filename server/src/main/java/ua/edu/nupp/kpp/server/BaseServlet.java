package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Спільна поведінка Servlet-ів: JSON-відповіді, розбір HTTP-параметрів і єдина обробка помилок.
 * Успішна відповідь — JSON, помилка — HTTP 400 (або 500) з JSON-об'єктом {"error": "..."}.
 */
public abstract class BaseServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());
    protected static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    protected abstract void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    protected abstract void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(req, resp, true);
    }

    private void run(HttpServletRequest req, HttpServletResponse resp, boolean post) throws IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            if (post) {
                handlePost(req, resp);
            } else {
                handleGet(req, resp);
            }
        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLIntegrityConstraintViolationException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Запис пов'язаний з іншими даними або дублює унікальне значення");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Помилка обробки запиту", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка сервера: " + e.getMessage());
        }
    }

    protected void sendJson(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }

    protected void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        sendJson(resp, Collections.singletonMap("error", message));
    }

    protected static String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected static long longParam(HttpServletRequest req, String name, boolean required) {
        String v = str(req, name);
        if (v.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("Параметр " + name + " є обов'язковим");
            }
            return 0;
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути цілим числом");
        }
    }

    protected static double doubleParam(HttpServletRequest req, String name) {
        try {
            return Double.parseDouble(str(req, name).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути числом");
        }
    }

    protected static BigDecimal decimalParam(HttpServletRequest req, String name) {
        try {
            return new BigDecimal(str(req, name).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути числом");
        }
    }

    protected static LocalDate dateParam(HttpServletRequest req, String name) {
        try {
            return LocalDate.parse(str(req, name));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Параметр " + name + " має мати формат РРРР-ММ-ДД");
        }
    }

    protected static boolean isDelete(HttpServletRequest req) {
        return "delete".equals(str(req, "action"));
    }

    protected static java.util.Map<String, Object> ok(long id) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("status", "ok");
        m.put("id", id);
        return m;
    }
}
