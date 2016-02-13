package anastasia.example;

import java.util.Set;

/**
 * Created by anastasiaknyazeva on 2/12/16.
 */
public class Group {
    private Long groupId;
    private Set<Long> permissions;
    private Set<Long> roles;

    public Group(Long groupId, Set<Long> permissions, Set<Long> roles) {
        this.groupId = groupId;
        this.permissions = permissions;
        this.roles = roles;
    }

    public Set<Long> addToSet(Set<Long> newMembers, boolean isPermission) {
        //isPermission : this.permissions.addAll(newMembers) ? this.roles.addAll(newMembers);

        return this.permissions;
    }


}
