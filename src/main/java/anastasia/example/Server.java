package anastasia.example;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.shiro.authz.permission.WildcardPermission;
import java.util.Map;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle{
    private static String PERMISSION_MAP = "permissions";
    private Map<Integer,WildcardPermission> permissions;


    @Override
    public void start() throws Exception{

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        permissions = client.getMap(PERMISSION_MAP);


        router.route().handler(BodyHandler.create());
        router.get("/hello/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end(" World!!");
        });
        router.get("/permissions/:permissionId").handler(this::handleGetPermission);
        router.put("/permissions/:permissionId").handler(this::handleAddPermission);
        router.get("/permissions").handler(this::handleListPermissions);

        router.get("/hello/").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.end(" World!!");
        });

        /*router.get("/permissions/:permissionId").handler(ctx -> {
            String permissionId = ctx.request().getParam("permissionId");
            //ctx.response().end(hazelcastInstance.getMap("permissions").get(permissionId).toString());


        });*/



        server.requestHandler(router::accept).listen(8081);
    }

    private void handleListPermissions(RoutingContext routingContext) {
        String arr = "";
        for (Integer permissionId : permissions.keySet() ) {
            arr += permissions.get(permissionId).toString() + ", ";
        }
        routingContext.response()
                //.putHeader("content-type", "application/json")
                .end(arr);

    }

    private void handleAddPermission(RoutingContext routingContext) {
        Integer permissionID = Integer.parseInt(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            String permissionString = routingContext.getBodyAsString();
            if (permissionString == null) {
                sendError(400, response);
            } else {
                permissions.put(permissionID, new WildcardPermission(permissionString));
                response.end("New permission was add to our list");
            }
        }

    }

    private void handleGetPermission(RoutingContext routingContext) {
        Integer permissionID = Integer.parseInt(routingContext.request().getParam("permissionId"));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermission permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                response.end(permission.toString());
            }
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end("STATUS CODE 400");
    }


}
