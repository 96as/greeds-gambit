package com.greedsgambit.score;

import java.time.Duration;

/** A player's fastest completion of a level. */
public record BestTime(String username, int levelNumber, Duration time) {
}
