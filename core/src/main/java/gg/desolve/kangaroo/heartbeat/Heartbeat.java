package gg.desolve.kangaroo.heartbeat;

import com.google.gson.Gson;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.storage.RedisStorage;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {

    private static final int TTL_SECONDS = 15;
    private static final int INTERVAL_SECONDS = 5;

    private final RedisStorage redis;
    private final Gson gson;
    @Getter
    private final Server server;
    private final ScheduledExecutorService executor;

    public Heartbeat(RedisStorage redis, Server server) {
        this.redis = redis;
        this.gson = new Gson();
        this.server = server;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        publish();
        executor.scheduleAtFixedRate(
                this::publish,
                INTERVAL_SECONDS,
                INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public void stop() {
        executor.shutdown();
        try {
            redis.execute(jedis ->
                    jedis.del("kangaroo:servers:" + server.getId()));
        } catch (Exception ignored) {
        }
    }

    private void publish() {
        try {
            server.setLastHeartbeat(System.currentTimeMillis());

            redis.execute(jedis ->
                    jedis.setex(
                            "kangaroo:servers:" + server.getId(),
                            TTL_SECONDS,
                            gson.toJson(server))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
