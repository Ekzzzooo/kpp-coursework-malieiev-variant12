package ua.edu.nupp.kpp.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /orders: GET — список; POST — створення/редагування;
 * POST action=delete — видалення; POST action=transition&state=... — зміна стану.
 */
public class RentalOrderServlet extends BaseServlet {
    private final RentalOrderService service = new RentalOrderService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        sendJson(resp, service.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = str(req, "action");
        if ("delete".equals(action)) {
            long id = longParam(req, "id", true);
            service.delete(id);
            sendJson(resp, ok(id));
            return;
        }
        if ("transition".equals(action)) {
            long id = longParam(req, "id", true);
            OrderState to;
            try {
                to = OrderState.valueOf(str(req, "state"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Невідомий стан заявки: " + str(req, "state"));
            }
            sendJson(resp, service.changeState(id, to));
            return;
        }
        RentalOrder o = new RentalOrder();
        o.setId(longParam(req, "id", false));
        o.setClientId(longParam(req, "clientId", true));
        o.setEquipmentId(longParam(req, "equipmentId", true));
        o.setStartDate(dateParam(req, "startDate"));
        o.setEndDate(dateParam(req, "endDate"));
        o.setF2(doubleParam(req, "f2"));
        o.setF3(doubleParam(req, "f3"));
        sendJson(resp, o.getId() > 0 ? service.update(o) : service.add(o));
    }
}
