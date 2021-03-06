package anastasia.example;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;

import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.Map;
import java.util.Objects;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by anastasiaknyazeva on 2/11/16.
 */
public class PermissionController {
    private static final String PERM_MAP = "permissions";
    private static final String PERM_ID = "permissionId";
    private static final String PERM_STR = "permission";


    public static void handleDeletePermission(RoutingContext routingContext) {
        Map<Long, WildcardPermissionDTO> permissions = HazelcastConnection.getClient().getMap(PERM_MAP);
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermissionDTO permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                permissions.remove(permissionID);
                response.end("Permission was deleted");
            }
        }

    }

    public static void handleListPermissions(RoutingContext routingContext) {
        Map<Long, WildcardPermissionDTO> permissions = HazelcastConnection.getClient().getMap(PERM_MAP);
        JsonArray arr = new JsonArray();
        for (Long permissionId : permissions.keySet() ) {
            arr.add(permissions.get(permissionId).toJson());
        }
        routingContext.response()
                .putHeader("Content-Type", "application/json")
                .end(arr.encodePrettily());

    }

    public static void handleAddPermission(RoutingContext routingContext) {
        Map<Long, WildcardPermissionDTO> permissions = HazelcastConnection.getClient().getMap(PERM_MAP);
        HttpServerResponse response = routingContext.response();
        Buffer body = routingContext.getBody();
        if (parameterCheck(body)) {
            sendError(400, response);
        }
        else {
            JsonObject permissionJson = body.toJsonObject();
            Long permissionId = HazelcastConnection.getIdGen().newId();
            WildcardPermissionDTO permission = new WildcardPermissionDTO(permissionId, permissionJson.getString(PERM_STR));
            permissions.put(permissionId, permission);
            //JsonObject permissionJsonNew = new JsonObject().put(PERM_ID, permissionId).put(PERM_STR, permission.toString());
            response.putHeader("Content-Type", "application/json")
                    .end(permission.toJson().toString());

        }


    }

    public static void handleUpdatePermission(RoutingContext routingContext) {
        Map<Long, WildcardPermissionDTO> permissions = HazelcastConnection.getClient().getMap(PERM_MAP);
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
        HttpServerResponse response = routingContext.response();
        Buffer body = routingContext.getBody();
        if (permissionID == null || parameterCheck(body)) {
            sendError(400, response);
        } else {
            WildcardPermissionDTO permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                String permissionString = routingContext.getBodyAsJson().getValue(PERM_STR).toString();
                WildcardPermissionDTO newPermission =  new WildcardPermissionDTO(permissionID, permissionString);
                permissions.replace(permissionID, newPermission);
                //JsonObject permissionJson = new JsonObject().put(PERM_ID, permissionID).put(PERM_STR, newPermission.toString());
                response.putHeader("Content-Type", "application/json").end(newPermission.toJson().toString());
            }
        }

    }
    public static void handleGetPermission(RoutingContext routingContext) {
        Map<Long, WildcardPermissionDTO> permissions = HazelcastConnection.getClient().getMap(PERM_MAP);
        Long permissionID = Long.parseLong(routingContext.request().getParam(PERM_ID));
        HttpServerResponse response = routingContext.response();
        if (permissionID == null) {
            sendError(400, response);
        } else {
            WildcardPermissionDTO permission = permissions.get(permissionID);
            if (permission == null) {
                sendError(400, response);
            } else {
                //JsonObject permissionJson = new JsonObject().put(PERM_ID, permissionID).put(PERM_STR,permission.toString());
                response.putHeader("Content-Type", "application/json").end(permission.toJson().toString());
            }
        }

    }

    private static void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end("Ups, error...");
    }

    private static boolean parameterCheck(Buffer body) {
        return  (body.toString().isEmpty() || body.toJsonObject().getValue(PERM_STR) == null);
    }
}
