package com.greedsgambit.game;

/**
 * The player's intent for one simulation step, independent of any keyboard or UI toolkit.
 *
 * @param interact true only on the step right after the interact key was pressed
 */
public record InputSnapshot(boolean up, boolean down, boolean left, boolean right, boolean run, boolean interact) {

    public static final InputSnapshot NONE = new InputSnapshot(false, false, false, false, false, false);
}
