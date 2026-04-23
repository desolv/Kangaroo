package gg.desolve.kangaroo.util;

import java.util.concurrent.TimeUnit;

public final class Duration {

    public static final Duration ZERO = new Duration(0L);

    private final long millis;

    private Duration(long millis) {
        this.millis = millis;
    }

    public static Duration ofMillis(long millis) {
        return new Duration(millis);
    }

    public static Duration ofSeconds(long seconds) {
        return new Duration(TimeUnit.SECONDS.toMillis(seconds));
    }

    public static Duration ofMinutes(long minutes) {
        return new Duration(TimeUnit.MINUTES.toMillis(minutes));
    }

    public static Duration ofHours(long hours) {
        return new Duration(TimeUnit.HOURS.toMillis(hours));
    }

    public static Duration ofDays(long days) {
        return new Duration(TimeUnit.DAYS.toMillis(days));
    }

    public static Duration parse(String input) {
        return new Duration(TimeUtil.parseDuration(input));
    }

    public long toMillis() {
        return millis;
    }

    public long toSeconds() {
        return millis / 1000L;
    }

    public boolean isPositive() {
        return millis > 0L;
    }

    public boolean isGreaterThan(Duration other) {
        return millis > other.millis;
    }

    public String format() {
        return TimeUtil.formatDuration(millis);
    }

    @Override
    public String toString() {
        return format();
    }
}
