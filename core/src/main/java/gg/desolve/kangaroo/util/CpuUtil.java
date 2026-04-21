package gg.desolve.kangaroo.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public final class CpuUtil {

    private static final OperatingSystemMXBean OS_BEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static double processLoadPercent() {
        double load = OS_BEAN.getProcessCpuLoad();
        if (load < 0) return 0.0;
        return load * 100.0;
    }
}
