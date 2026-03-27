package gg.desolve.kangaroo.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

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
