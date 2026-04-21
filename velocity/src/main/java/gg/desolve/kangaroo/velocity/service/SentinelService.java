package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.velocity.KangarooVelocity;
import lombok.Getter;
import redis.clients.jedis.params.SetParams;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SentinelService {

    private final ScheduledExecutorService executor;

    @Getter
    private volatile boolean isSentinel;

    public SentinelService() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-sentinel");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
        try {
            KangarooVelocity plugin = KangarooVelocity.getInstance();
            String myId = plugin.getProxyId();
            plugin.getRedisStorage().execute(jedis -> {
                if (myId.equals(jedis.get("kangaroo:sentinel"))) {
                    jedis.del("kangaroo:sentinel");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isSentinel = false;
    }

    private void tick() {
        try {
            KangarooVelocity plugin = KangarooVelocity.getInstance();
            String myId = plugin.getProxyId();

            this.isSentinel = plugin.getRedisStorage().query(jedis -> {
                String current = jedis.get("kangaroo:sentinel");
                if (myId.equals(current)) {
                    jedis.expire("kangaroo:sentinel", 15);
                    return true;
                }
                if (current == null) {
                    return "OK".equals(jedis.set("kangaroo:sentinel", myId, SetParams.setParams().nx().ex(15)));
                }
                return false;
            });
        } catch (Exception e) {
            this.isSentinel = false;
            e.printStackTrace();
        }
    }
}
