package anastasia.example;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
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
    private static String PERMISSION_MAP = "permissions";
    private Map<Long, WildcardPermission> permissions;
    private IdGenerator idGen;

    @Override
    public void start() throws Exception{

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        permissions = client.getMap(PERMISSION_MAP);
        idGen = client.getIdGenerator( "newId" );


        router.route().handler(BodyHandler.create());
        router.get("/hello/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end(" World!!");
        });
        router.get("/permissions/:permissionId").handler(this::handleGetPermission);
        router.delete("/permissions/:permissionId").handler(this::handleDeletePermission);
        router.put("/permissions/:permissionId").handler(this::handleUpdatePermission);
        router.post("/permissions").handler(this::handleAddPermission);
        router.get("/permissions").handler(this::handleListPermissions);

        router.get().handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end("Hello World!!");
        });
        server.requestHandler(router::accept).listen(8081);
    }

    private void handleDeletePermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                permissions.remove(permissionID);
                response.end("Permission was deleted");
            }
        }

    }

    private void handleListPermissions(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        for (Long permissionId : permissions.keySet() ) {
            arr.add(new JsonObject().put("permissionId",permissionId).put("permission",permissions.get(permissionId).toString()));
        }
        routingContext.response()
                .putHeader("Content-Type", "application/json")
                .end(arr.encodePrettily());

    }

    private void handleAddPermission(RoutingContext routingContext) {
        //Integer permissionID = Integer.parseInt(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (routingContext.getBodyAsString().isEmpty() || routingContext.getBodyAsJson().getValue("permission") == null) {
            sendError(400, response);
        }
        else {
            JsonObject permissionJson = routingContext.getBodyAsJson();
            Long newId = idGen.newId();
            WildcardPermission permission = new WildcardPermission(permissionJson.getValue("permission").toString());
            permissions.put(newId, permission);
            JsonObject permissionJsonNew = new JsonObject().put("permissionId", newId).put("permission", permission.toString());
            response.putHeader("Content-Type", "application/json").end(permissionJsonNew.toString());

        }


    }

    private void handleUpdatePermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null || routingContext.getBodyAsString().isEmpty()
                || routingContext.getBodyAsJson().getValue("permission") == null) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                String permissionString = routingContext.getBodyAsJson().getValue("permission").toString();
                WildcardPermission newPermission =  new WildcardPermission(permissionString);
                permissions.replace(permissionID, newPermission);
                JsonObject permissionJson = new JsonObject().put("permissionId", permissionID).put("permission", newPermission.toString());
                response.putHeader("content-type", "application/json").end(permissionJson.toString());
            }
        }

    }
    private void handleGetPermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                JsonObject permissionJson = new JsonObject().put("permissionId", permissionID).put("permission",permission.toString());
                response.putHeader("Content-Type", "application/json").end(permissionJson.toString());
            }
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end("Ups, error...");
    }


}
