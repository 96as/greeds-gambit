package com.greedsgambit.util;

import java.time.Duration;

public final class TimeFormat {

    private TimeFormat() {
    }

    /** {@code 83.9 s -> "01:23"}. Minutes keep counting past 59 rather than rolling into hours. */
    public static String minutesSeconds(Duration duration) {
        long totalSeconds = Math.max(0, duration.toSeconds());
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public static Duration ofSeconds(double seconds) {
        return Duration.ofNanos(Math.round(seconds * 1_000_000_000L));
    }
}
