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
    private static String PERM_MAP = "permissions";
    private static String PERM_PATH = "/permissions/";
    private static String PERM_ID = "permissionId";
    private static String PERM_STR = "permission";
    private Map<Long, WildcardPermission> permissions;
    private IdGenerator idGen;

    @Override
    public void start() throws Exception{

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        permissions = client.getMap(PERM_MAP);
        idGen = client.getIdGenerator( "newId" );


        router.route().handler(BodyHandler.create());
        router.get("/hello/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.end(" World!!");
        });
        router.get(PERM_PATH + ":" + PERM_ID).handler(this::handleGetPermission);
        router.delete(PERM_PATH + ":" + PERM_ID).handler(this::handleDeletePermission);
        router.put(PERM_PATH + ":" + PERM_ID).handler(this::handleUpdatePermission);
        router.post(PERM_PATH).handler(this::handleAddPermission);
        router.get(PERM_PATH).handler(this::handleListPermissions);

        router.get().handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end("Hello World!!");
        });
        server.requestHandler(router::accept).listen(8081);
    }

    private void handleDeletePermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
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
            arr.add(new JsonObject().put(PERM_ID,permissionId).put(PERM_STR,permissions.get(permissionId).toString()));
        }
        routingContext.response()
                .putHeader("Content-Type", "application/json")
                .end(arr.encodePrettily());

    }

    private void handleAddPermission(RoutingContext routingContext) {
        //Integer permissionID = Integer.parseInt(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        Buffer body = routingContext.getBody();
        if (parameterCheck(body)) {
            sendError(400, response);
        }
        else {
            JsonObject permissionJson = body.toJsonObject();
            Long permissionId = idGen.newId();
            WildcardPermission permission = new WildcardPermission(permissionJson.getValue(PERM_STR).toString());
            permissions.put(permissionId, permission);
            JsonObject permissionJsonNew = new JsonObject().put(PERM_ID, permissionId).put(PERM_STR, permission.toString());
            response.putHeader("Content-Type", "application/json")
                    .end(permissionJsonNew.toString());

        }


    }

    private void handleUpdatePermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
        HttpServerResponse response = routingContext.response();
        Buffer body = routingContext.getBody();
        if (permissionID == null || parameterCheck(body)) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                String permissionString = routingContext.getBodyAsJson().getValue(PERM_STR).toString();
                WildcardPermission newPermission =  new WildcardPermission(permissionString);
                permissions.replace(permissionID, newPermission);
                JsonObject permissionJson = new JsonObject().put(PERM_ID, permissionID).put(PERM_STR, newPermission.toString());
                response.putHeader("Content-Type", "application/json").end(permissionJson.toString());
            }
        }

    }
    private void handleGetPermission(RoutingContext routingContext) {
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                JsonObject permissionJson = new JsonObject().put(PERM_ID, permissionID).put(PERM_STR,permission.toString());
                response.putHeader("Content-Type", "application/json").end(permissionJson.toString());
            }
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end("Ups, error...");
    }

    private boolean parameterCheck(Buffer body) {
        return  (body.toString().isEmpty() || body.toJsonObject().getValue(PERM_STR) == null);
    }

}
