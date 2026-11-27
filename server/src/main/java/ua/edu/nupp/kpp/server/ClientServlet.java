package ua.edu.nupp.kpp.server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** /clients: GET — список; POST — створення/редагування; POST action=delete — видалення. */
public class ClientServlet extends BaseServlet {
    private final ClientService service = new ClientService();

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
        Client c = new Client();
        c.setId(longParam(req, "id", false));
        c.setFullName(str(req, "fullName"));
        c.setPhone(str(req, "phone"));
        c.setEmail(str(req, "email"));
        c.setAddress(str(req, "address"));
        sendJson(resp, c.getId() > 0 ? service.update(c) : service.add(c));
    }
}
