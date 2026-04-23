package gg.desolve.kangaroo.reboot;

import gg.desolve.kangaroo.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledReboot {

    private ScheduleType type;
    private String target;
    private String dailyTime;
    private Long runAt;
    private long createdAt;

    public long nextFireAt(long now) {
        if (type != ScheduleType.DAILY)
            return runAt == null ?
                    Long.MAX_VALUE :
                    runAt;

        ZonedDateTime today = TimeUtil.todayAt(dailyTime);
        long todayMillis = today.toInstant().toEpochMilli();

        return now <= todayMillis + 1_000L ?
                todayMillis :
                today.plusDays(1).toInstant().toEpochMilli();
    }
}
