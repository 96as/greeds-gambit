package com.greedsgambit.game.level;

import com.greedsgambit.game.geometry.Vec2;

import java.util.List;
import java.util.Objects;

/**
 * A goblin guard that walks back and forth along {@code route}.
 *
 * @param route            waypoints for the guard's centre; walked forwards then backwards, forever
 * @param speed            world units per second
 * @param visionRange      how far the guard can see
 * @param visionHalfAngle  half the width of the field of view, in degrees
 */
public record GuardSpec(String id, List<Vec2> route, double speed, double visionRange, double visionHalfAngle) {

    public GuardSpec {
        Objects.requireNonNull(id, "id");
        route = List.copyOf(route);
        if (route.size() < 2) {
            throw new IllegalArgumentException("Guard " + id + " needs at least two waypoints");
        }
        for (int i = 1; i < route.size(); i++) {
            if (route.get(i).equals(route.get(i - 1))) {
                throw new IllegalArgumentException("Guard " + id + " has a repeated waypoint at index " + i);
            }
        }
        if (speed <= 0) {
            throw new IllegalArgumentException("Guard " + id + " needs a positive speed");
        }
    }
}
