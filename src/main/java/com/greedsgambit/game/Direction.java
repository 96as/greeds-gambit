package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Vec2;

/** The four directions a guard sprite can face. */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /** The closest of the four directions to {@code heading}; ties go to the horizontal. */
    public static Direction of(Vec2 heading) {
        if (Math.abs(heading.x()) >= Math.abs(heading.y())) {
            return heading.x() < 0 ? LEFT : RIGHT;
        }
        return heading.y() < 0 ? UP : DOWN;
    }
}
