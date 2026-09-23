package com.greedsgambit.game.level;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;

import java.util.List;
import java.util.Objects;

/**
 * Everything that makes up one level, as plain data. The game rules in
 * {@link com.greedsgambit.game.GameSession} work for any level described this way.
 *
 * @param number      1-based position in the campaign; also the key for the level's artwork
 * @param bounds      the playable area; the player can never leave it
 * @param playerSpawn top-left corner of the player at the start and after being caught
 * @param walls       static solid areas
 * @param exitZone    where the player must stand to finish the level
 */
public record LevelDefinition(
        int number,
        String name,
        Rect bounds,
        Vec2 playerSpawn,
        List<Rect> walls,
        List<DoorSpec> doors,
        List<GuardSpec> guards,
        Rect exitZone) {

    public LevelDefinition {
        if (number < 1) {
            throw new IllegalArgumentException("Level numbers start at 1");
        }
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(playerSpawn, "playerSpawn");
        Objects.requireNonNull(exitZone, "exitZone");
        walls = List.copyOf(walls);
        doors = List.copyOf(doors);
        guards = List.copyOf(guards);
    }
}
