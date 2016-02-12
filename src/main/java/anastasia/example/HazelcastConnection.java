package anastasia.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.com.eclipsesource.json.JsonObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;

/**
 * Created by anastasiaknyazeva on 2/11/16.
 */
public class HazelcastConnection {
    private static HazelcastConnection instance = null;
    private HazelcastInstance client;
    private IdGenerator idGen;

    protected HazelcastConnection() {
        ClientConfig clientConfig = new ClientConfig();
        this.client = HazelcastClient.newHazelcastClient( clientConfig);
        this.idGen = client.getIdGenerator( "newId" );
        //create one test role for add/delete/update its permissions
        client.getMap("roles").put(1,new JsonObject().add("roleId", 1).add("roleName", "TEST_ROLE").add("permissions","{}"));
    }

    public static HazelcastInstance getClient () {
        if (instance == null) {
            instance = new HazelcastConnection();
        }
        return instance.client;
    }

    public static IdGenerator getIdGen() {
        if (instance == null) {
            instance = new HazelcastConnection();
        }
        return instance.idGen;
    }
}
