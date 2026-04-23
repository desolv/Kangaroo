package gg.desolve.kangaroo.reboot;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScheduledRebootService {

    private static final String DEFAULT_DAILY_TIME = "01:00";

    private final RedisStorage redisStorage;

    public ScheduledRebootService(RedisStorage redisStorage) {
        this.redisStorage = redisStorage;
    }

    public void set(ScheduledReboot scheduledReboot) {
        redisStorage.execute(jedis -> jedis.set(keyFor(scheduledReboot.getType()), JsonUtil.GSON.toJson(scheduledReboot)));
    }

    public Optional<ScheduledReboot> get(ScheduleType type) {
        String json = redisStorage.query(jedis -> jedis.get(keyFor(type)));
        if (json != null)
            return Optional.of(JsonUtil.GSON.fromJson(json, ScheduledReboot.class));
        if (type == ScheduleType.DAILY)
            return Optional.of(defaultDaily());
        return Optional.empty();
    }

    public boolean hasOverride(ScheduleType type) {
        return redisStorage.query(jedis -> jedis.get(keyFor(type))) != null;
    }

    private static ScheduledReboot defaultDaily() {
        return new ScheduledReboot(
                ScheduleType.DAILY,
                "global",
                DEFAULT_DAILY_TIME,
                null,
                0L
        );
    }

    public List<ScheduledReboot> getAll() {
        List<ScheduledReboot> result = new ArrayList<>(ScheduleType.values().length);
        for (ScheduleType type : ScheduleType.values()) {
            get(type).ifPresent(result::add);
        }
        return result;
    }

    public boolean delete(ScheduleType type) {
        Long removed = redisStorage.query(jedis -> jedis.del(keyFor(type)));
        return removed != null && removed > 0;
    }

    private static String keyFor(ScheduleType type) {
        return "kangaroo:reboot:" + type.name().toLowerCase();
    }
}
