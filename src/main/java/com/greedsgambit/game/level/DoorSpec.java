package com.greedsgambit.game.level;

import com.greedsgambit.game.geometry.Rect;

import java.util.Objects;

/**
 * A door that blocks the way while closed and is toggled by a lever.
 *
 * @param wall      solid area while the door is closed
 * @param leverZone where the player must stand to pull the lever
 */
public record DoorSpec(String id, Rect wall, Rect leverZone) {

    public DoorSpec {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(wall, "wall");
        Objects.requireNonNull(leverZone, "leverZone");
    }
}
