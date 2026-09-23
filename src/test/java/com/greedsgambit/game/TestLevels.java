package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;
import com.greedsgambit.game.level.DoorSpec;
import com.greedsgambit.game.level.GuardSpec;
import com.greedsgambit.game.level.LevelDefinition;

import java.util.List;

/** Small hand-made levels that make each rule easy to test in isolation. */
final class TestLevels {

    static final Rect BOUNDS = new Rect(0, 0, 400, 200);
    static final Vec2 SPAWN = new Vec2(10, 10);

    private TestLevels() {
    }

    static LevelDefinition empty() {
        return level(List.of(), List.of(), List.of(), new Rect(350, 150, 50, 50));
    }

    static LevelDefinition withWalls(Rect... walls) {
        return level(List.of(walls), List.of(), List.of(), new Rect(350, 150, 50, 50));
    }

    static LevelDefinition withDoor(DoorSpec door) {
        return level(List.of(), List.of(door), List.of(), new Rect(350, 150, 50, 50));
    }

    static LevelDefinition withGuards(List<Rect> walls, GuardSpec... guards) {
        return level(walls, List.of(), List.of(guards), new Rect(350, 150, 50, 50));
    }

    static LevelDefinition level(List<Rect> walls, List<DoorSpec> doors, List<GuardSpec> guards, Rect exit) {
        return new LevelDefinition(1, "Test", BOUNDS, SPAWN, walls, doors, guards, exit);
    }

    static InputSnapshot keys(boolean up, boolean down, boolean left, boolean right) {
        return new InputSnapshot(up, down, left, right, false, false);
    }

    static final InputSnapshot RIGHT = keys(false, false, false, true);
    static final InputSnapshot LEFT = keys(false, false, true, false);
    static final InputSnapshot DOWN = keys(false, true, false, false);
    static final InputSnapshot RUN_RIGHT = new InputSnapshot(false, false, false, true, true, false);
    static final InputSnapshot DOWN_RIGHT = keys(false, true, false, true);
    static final InputSnapshot INTERACT = new InputSnapshot(false, false, false, false, false, true);

    /** Runs {@code seconds} of game time in 1/120 s steps with the same input held. */
    static void hold(GameSession session, InputSnapshot input, double seconds) {
        int steps = (int) Math.round(seconds * 120);
        for (int i = 0; i < steps; i++) {
            session.update(1.0 / 120, input);
        }
    }
}
