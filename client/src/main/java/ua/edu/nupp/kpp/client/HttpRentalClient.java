package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Єдина точка взаємодії JavaFX-клієнта з сервером: формує HTTP-запити (GET, POST),
 * кодує параметри та перетворює JSON-відповіді на Java-об'єкти. SQL і MySQL клієнт не використовує.
 */
public class HttpRentalClient {
    private static final String BASE = "http://localhost:8080/equipment-rental-server";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ---------- Обладнання ----------
    public List<Equipment> getEquipment() {
        return Arrays.asList(read(get("/equipment"), Equipment[].class));
    }

    public Equipment addEquipment(Equipment e) {
        return read(post("/equipment", equipmentParams(e)), Equipment.class);
    }

    public Equipment updateEquipment(Equipment e) {
        Map<String, String> p = equipmentParams(e);
        p.put("id", String.valueOf(e.getId()));
        return read(post("/equipment", p), Equipment.class);
    }

    public void deleteEquipment(long id) {
        post("/equipment", deleteParams(id));
    }

    private Map<String, String> equipmentParams(Equipment e) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("code", e.getCode());
        p.put("name", e.getName());
        p.put("category", e.getCategory());
        p.put("pricePerDay", String.valueOf(e.getPricePerDay()));
        return p;
    }

    // ---------- Клієнти ----------
    public List<Client> getClients() {
        return Arrays.asList(read(get("/clients"), Client[].class));
    }

    public Client addClient(Client c) {
        return read(post("/clients", clientParams(c)), Client.class);
    }

    public Client updateClient(Client c) {
        Map<String, String> p = clientParams(c);
        p.put("id", String.valueOf(c.getId()));
        return read(post("/clients", p), Client.class);
    }

    public void deleteClient(long id) {
        post("/clients", deleteParams(id));
    }

    private Map<String, String> clientParams(Client c) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("fullName", c.getFullName());
        p.put("phone", c.getPhone());
        p.put("email", c.getEmail());
        p.put("address", c.getAddress());
        return p;
    }

    // ---------- Заявки ----------
    public List<RentalOrder> getOrders() {
        return Arrays.asList(read(get("/orders"), RentalOrder[].class));
    }

    public RentalOrder addOrder(RentalOrder o) {
        return read(post("/orders", orderParams(o)), RentalOrder.class);
    }

    public RentalOrder updateOrder(RentalOrder o) {
        Map<String, String> p = orderParams(o);
        p.put("id", String.valueOf(o.getId()));
        return read(post("/orders", p), RentalOrder.class);
    }

    public void deleteOrder(long id) {
        post("/orders", deleteParams(id));
    }

    public RentalOrder changeOrderState(long id, OrderState state) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("action", "transition");
        p.put("id", String.valueOf(id));
        p.put("state", state.name());
        return read(post("/orders", p), RentalOrder.class);
    }

    private Map<String, String> orderParams(RentalOrder o) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("clientId", String.valueOf(o.getClientId()));
        p.put("equipmentId", String.valueOf(o.getEquipmentId()));
        p.put("startDate", String.valueOf(o.getStartDate()));
        p.put("endDate", String.valueOf(o.getEndDate()));
        p.put("f2", String.valueOf(o.getF2()));
        p.put("f3", String.valueOf(o.getF3()));
        return p;
    }

    // ---------- Договори ----------
    public List<RentalContract> getContracts() {
        return Arrays.asList(read(get("/contracts"), RentalContract[].class));
    }

    public RentalContract addContract(long orderId, String number, LocalDate signDate) {
        return read(post("/contracts", contractParams(orderId, number, signDate)), RentalContract.class);
    }

    public RentalContract updateContract(long id, long orderId, String number, LocalDate signDate) {
        Map<String, String> p = contractParams(orderId, number, signDate);
        p.put("id", String.valueOf(id));
        return read(post("/contracts", p), RentalContract.class);
    }

    public void deleteContract(long id) {
        post("/contracts", deleteParams(id));
    }

    private Map<String, String> contractParams(long orderId, String number, LocalDate signDate) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("orderId", String.valueOf(orderId));
        p.put("contractNumber", number);
        p.put("signDate", String.valueOf(signDate));
        return p;
    }

    // ---------- Низькорівнева робота з HTTP ----------
    private Map<String, String> deleteParams(long id) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("action", "delete");
        p.put("id", String.valueOf(id));
        return p;
    }

    private String get(String path) {
        return send(HttpRequest.newBuilder(URI.create(BASE + path)).GET().build());
    }

    private String post(String path, Map<String, String> params) {
        String body = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new ApiException(response.statusCode(), errorMessage(response.body(), response.statusCode()));
            }
            return response.body();
        } catch (IOException e) {
            throw new ApiException(0, "Сервер недоступний. Перевірте, що Tomcat і MySQL запущені.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Запит перервано");
        }
    }

    private String errorMessage(String body, int status) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (IOException ignored) {
            // тіло відповіді не є JSON
        }
        return "Помилка сервера (HTTP " + status + ")";
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new ApiException(0, "Некоректна відповідь сервера: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
