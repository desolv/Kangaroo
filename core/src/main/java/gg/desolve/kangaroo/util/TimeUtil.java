package gg.desolve.kangaroo.util;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static String format(long epochMillis) {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(epochMillis));
    }

    public static long parseDuration(String input) {
        if (input == null || input.length() < 2) {
            throw new IllegalArgumentException("expected e.g. 30s, 15m, 2h, 1d");
        }
        char suffix = Character.toLowerCase(input.charAt(input.length() - 1));
        long value = Long.parseLong(input.substring(0, input.length() - 1));
        return switch (suffix) {
            case 's' -> TimeUnit.SECONDS.toMillis(value);
            case 'm' -> TimeUnit.MINUTES.toMillis(value);
            case 'h' -> TimeUnit.HOURS.toMillis(value);
            case 'd' -> TimeUnit.DAYS.toMillis(value);
            default -> throw new IllegalArgumentException("expected suffix s/m/h/d");
        };
    }

    public static ZonedDateTime todayAt(String hhmm) {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .with(LocalTime.parse(hhmm, DateTimeFormatter.ofPattern("HH:mm")));
    }

    public static String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder
                .append(days)
                .append(days == 1 ? " day " : " days ");
        if (hours > 0) builder
                .append(hours)
                .append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0) builder
                .append(minutes)
                .append(minutes == 1 ? " minute " : " minutes ");
        if (seconds > 0 || builder.isEmpty()) builder
                .append(seconds)
                .append(seconds == 1 ? " second" : " seconds");
        return builder.toString().trim();
    }

    public static String formatUptime(long startTime) {
        long millis = System.currentTimeMillis() - startTime;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m ").append(seconds).append("s");
        return sb.toString();
    }

    public static long secondsAgo(long timestamp) {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}
