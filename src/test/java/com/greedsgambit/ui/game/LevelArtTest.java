package com.greedsgambit.ui.game;

import com.greedsgambit.game.level.LevelDefinition;
import com.greedsgambit.game.level.Levels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LevelArtTest {

    private static final String IMAGES = "/com/greedsgambit/images/";

    @Test
    void everyLevelHasBackgroundArt() {
        for (LevelDefinition level : Levels.all()) {
            LevelArt art = LevelArt.forLevel(level.number());
            assertNotNull(getClass().getResource(IMAGES + art.doorsClosed()), art.doorsClosed());
            assertNotNull(getClass().getResource(IMAGES + art.doorsOpen()), art.doorsOpen());
        }
    }

    @Test
    void spritesAndThemeFilesAreBundled() {
        List<String> resources = List.of(
                IMAGES + "player/idle-left.gif", IMAGES + "player/idle-right.gif",
                IMAGES + "player/walk-left.gif", IMAGES + "player/walk-right.gif",
                IMAGES + "player/run-left.gif", IMAGES + "player/run-right.gif",
                IMAGES + "goblin/up.gif", IMAGES + "goblin/down.gif",
                IMAGES + "goblin/left.gif", IMAGES + "goblin/right.gif",
                IMAGES + "ui/heart.png", IMAGES + "ui/menu-background.gif",
                "/com/greedsgambit/css/theme.css",
                "/com/greedsgambit/fonts/connection-regular.otf",
                "/com/greedsgambit/fonts/connection-bold.otf");
        resources.forEach(resource -> assertNotNull(getClass().getResource(resource), resource));
    }
}
