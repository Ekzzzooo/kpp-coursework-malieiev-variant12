package ua.edu.nupp.kpp.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** /equipment: GET — список; POST — створення/редагування; POST action=delete — видалення. */
public class EquipmentServlet extends BaseServlet {
    private final EquipmentService service = new EquipmentService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        sendJson(resp, service.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (isDelete(req)) {
            long id = longParam(req, "id", true);
            service.delete(id);
            sendJson(resp, ok(id));
            return;
        }
        Equipment e = new Equipment();
        e.setId(longParam(req, "id", false));
        e.setCode(str(req, "code"));
        e.setName(str(req, "name"));
        e.setCategory(str(req, "category"));
        e.setPricePerDay(decimalParam(req, "pricePerDay"));
        sendJson(resp, e.getId() > 0 ? service.update(e) : service.add(e));
    }
}
