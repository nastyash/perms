package anastasia.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import org.apache.shiro.authz.permission.WildcardPermission;


/**
 * Created by anastasiaknyazeva on 2/10/16.
 */
public class RunHazelcast {
    public static void main( String[] args ) {
        Hazelcast.newHazelcastInstance();
    }
}
