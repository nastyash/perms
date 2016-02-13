package anastasia.example;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle{
    private static final String API_V1_ROOT = "/"; //"/mp-api/v1";

    private static final String PERM_PATH = "/permissions/";
    private static final String PERM_ID = "permissionId";

    private static final String GROUP_PATH = "/groups/";
    private static final String GROUP_ID = "groupId";

    @Override
    public void start() throws Exception{

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        Router v1 = Router.router(vertx);
        router.mountSubRouter(API_V1_ROOT, v1);

        v1.route().handler(BodyHandler.create());
        v1.get("/hello/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.end(" World!!");
        });
        v1.get(PERM_PATH + ":" + PERM_ID).handler(PermissionController::handleGetPermission);
        v1.delete(PERM_PATH + ":" + PERM_ID).handler(PermissionController::handleDeletePermission);
        v1.put(PERM_PATH + ":" + PERM_ID).handler(PermissionController::handleUpdatePermission);
        v1.post(PERM_PATH).handler(PermissionController::handleAddPermission);
        v1.get(PERM_PATH).handler(PermissionController::handleListPermissions);

        v1.get(GROUP_PATH).handler(GroupController::handleListGroups);
        v1.route().method(HttpMethod.GET).handler(GroupController::hadleGetGroup);
        v1.route().method(HttpMethod.POST).handler(GroupController::handleAddGroup);
        v1.route().method(HttpMethod.DELETE).handler(GroupController::handleDeleteGroup);
        v1.route().method(HttpMethod.PUT).handler(GroupController::handleUpdateGroup);

        v1.get().handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end("Hello World!!");
        });
        server.requestHandler(router::accept).listen(8081);
    }

}
