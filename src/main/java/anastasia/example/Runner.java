package anastasia.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

import io.vertx.core.Vertx;

/**
 * Created by anastasiaknyazeva on 2/10/16.
 */
public class Runner {
    private static Vertx vertx;

    public static void main(String[] args) {
        new Runner().run();
    }

    public void run() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(Server.class.getName());

    }
}
