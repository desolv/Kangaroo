package gg.desolve.kangaroo.heartbeat;

import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerEvent;
import gg.desolve.kangaroo.server.ServerEventType;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import lombok.Getter;

public class Heartbeat {

    private final RedisStorage redis;
    @Getter
    private final Server server;
    private final long loadStartTime;
    private final KangarooScheduler scheduler;
    private KangarooScheduler.ScheduledTask task;

    public Heartbeat(RedisStorage redis, Server server, long loadStartTime, KangarooScheduler scheduler) {
        this.redis = redis;
        this.server = server;
        this.loadStartTime = loadStartTime;
        this.scheduler = scheduler;
    }

    public void start() {
        publish();
        publishServerEvent(ServerEventType.CONNECTED, 0);
        this.task = scheduler.scheduleRepeating(this::publish, 5, 5);
    }

    public void markLoaded() {
        publishServerEvent(ServerEventType.LOADED, System.currentTimeMillis() - loadStartTime);
    }

    public void stop() {
        if (task != null) task.cancel();
        try {
            publishServerEvent(ServerEventType.DISCONNECTED, 0);
            redis.execute(jedis -> jedis.del("kangaroo:servers:" + server.getId()));
        } catch (Exception ignored) {
        }
    }

    private void publish() {
        try {
            server.setLastHeartbeat(System.currentTimeMillis());
            redis.execute(jedis ->
                    jedis.setex("kangaroo:servers:" + server.getId(), 15, JsonUtil.GSON.toJson(server)));
        } catch (Exception e) {
            System.err.println("[Kangaroo] Heartbeat publish failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void publishServerEvent(ServerEventType type, long loadTimeMs) {
        try {
            ServerEvent event = new ServerEvent(
                    type,
                    server.getId(),
                    server.getType(),
                    loadTimeMs,
                    System.currentTimeMillis()
            );
            redis.publish("kangaroo:server-events", JsonUtil.GSON.toJson(event));
        } catch (Exception ignored) {
        }
    }
}
