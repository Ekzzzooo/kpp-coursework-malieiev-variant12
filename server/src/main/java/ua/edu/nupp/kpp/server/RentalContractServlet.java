package ua.edu.nupp.kpp.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** /contracts: GET — список; POST — створення/редагування; POST action=delete — видалення. */
public class RentalContractServlet extends BaseServlet {
    private final RentalContractService service = new RentalContractService();

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
        RentalContract k = new RentalContract();
        k.setId(longParam(req, "id", false));
        k.setOrderId(longParam(req, "orderId", true));
        k.setContractNumber(str(req, "contractNumber"));
        k.setSignDate(dateParam(req, "signDate"));
        sendJson(resp, k.getId() > 0 ? service.update(k) : service.add(k));
    }
}
