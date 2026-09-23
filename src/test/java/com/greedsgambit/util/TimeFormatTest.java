package com.greedsgambit.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeFormatTest {

    @Test
    void formatsMinutesAndSeconds() {
        assertEquals("00:00", TimeFormat.minutesSeconds(Duration.ZERO));
        assertEquals("01:23", TimeFormat.minutesSeconds(TimeFormat.ofSeconds(83.9)));
        assertEquals("75:00", TimeFormat.minutesSeconds(Duration.ofMinutes(75)));
    }
}
