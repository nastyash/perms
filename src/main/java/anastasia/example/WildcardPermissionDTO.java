package anastasia.example;

import org.apache.shiro.authz.permission.WildcardPermission;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Created by anastasiaknyazeva on 2/11/16.
 */
public class WildcardPermissionDTO extends WildcardPermission {
    private Long permissionId;

    public WildcardPermissionDTO (Long permissionId, String permission) {
        super(permission);
        this.permissionId = permissionId;
    }
    public JsonObject toJson() {
       // this.getParts().forEach();
        return new JsonObject().put("permissionId", this.permissionId).put("permission", this.toString());
    }
}
