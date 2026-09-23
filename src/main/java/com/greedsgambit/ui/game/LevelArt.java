package com.greedsgambit.ui.game;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Background images per level. Kept out of {@link com.greedsgambit.game.level.LevelDefinition}
 * so the game rules do not depend on file names or artwork.
 *
 * @param doorsClosed image shown while every door is shut
 * @param doorsOpen   image shown once a door has been opened (the same image for levels without doors)
 */
record LevelArt(String doorsClosed, String doorsOpen) {

    private static final Map<Integer, LevelArt> BY_LEVEL = Map.of(
            1, new LevelArt("levels/level1-door-closed.jpg", "levels/level1-door-open.jpg"),
            2, new LevelArt("levels/level2.jpg", "levels/level2.jpg"));

    static LevelArt forLevel(int levelNumber) {
        LevelArt art = BY_LEVEL.get(levelNumber);
        if (art == null) {
            throw new NoSuchElementException("No artwork registered for level " + levelNumber);
        }
        return art;
    }
}
