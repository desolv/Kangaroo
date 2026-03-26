package gg.desolve.kangaroo.heartbeat;

import com.google.gson.Gson;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.storage.RedisStorage;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {

    private static final String KEY_PREFIX = "kangaroo:servers:";
    private static final int TTL_SECONDS = 10;
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
        executor.scheduleAtFixedRate(this::publish, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
        try {
            redis.execute(jedis -> jedis.del(KEY_PREFIX + server.getId()));
        } catch (Exception ignored) {
        }
    }

    private void publish() {
        try {
            String key = KEY_PREFIX + server.getId();
            String json = gson.toJson(server);

            redis.execute(jedis -> {
                jedis.setex(key, TTL_SECONDS, json);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
