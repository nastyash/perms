package anastasia.example;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.shiro.authz.permission.WildcardPermission;
import java.util.Map;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle{
    private static final String PERM_PATH = "/permissions/";
    private static final String PERM_ID = "permissionId";
    private static final String API_V1_ROOT = "/"; //"/mp-api/v1";

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

        v1.delete("/roles/:roleId/permissions/:permissionId").handler(PermissionController::handleDeleteRolePermission);
        v1.post("/roles/:roleId/permissions/").handler(PermissionController::handleAddRolePermission);
        v1.put("/roles/:roleId/permissions/:permissionId").handler(PermissionController::handleChangeRolePermission);

        v1.get("/groups/").handler(GroupController::handleListGroups);

        v1.get().handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end("Hello World!!");
        });
        server.requestHandler(router::accept).listen(8081);
    }

}
