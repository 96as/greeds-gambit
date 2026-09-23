package com.greedsgambit.game.level;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * The campaign. Coordinates are in the 800x400 world the level art is drawn for, and were
 * traced over the background images (see docs/ARCHITECTURE.md, "Level data").
 */
public final class Levels {

    public static final Rect WORLD = new Rect(0, 0, 800, 400);

    private static final List<LevelDefinition> ALL = List.of(level1(), level2());

    private Levels() {
    }

    public static List<LevelDefinition> all() {
        return ALL;
    }

    public static LevelDefinition byNumber(int number) {
        return ALL.stream()
                .filter(level -> level.number() == number)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No level " + number));
    }

    public static boolean exists(int number) {
        return ALL.stream().anyMatch(level -> level.number() == number);
    }

    /** The library: pull the lever to open the study door, then sneak past the goblins to the stairs. */
    private static LevelDefinition level1() {
        List<Rect> walls = List.of(
                wall(30, 33, 780, 33, 1),     // outer wall - top
                wall(33, 35, 33, 350, 1),     // outer wall - left
                wall(50, 353, 780, 353, 1),   // outer wall - bottom
                wall(767, 33, 767, 345, 1),   // outer wall - right
                wall(300, 31, 300, 151, 3),   // wall between the start room and the corridor
                wall(417, 154, 417, 345, 3),  // wall between the start room and the library
                wall(235, 235, 235, 345, 5),  // fence - right post
                wall(60, 262, 140, 262, 9),   // fence - left rail
                wall(195, 262, 260, 262, 9),  // fence - right rail
                wall(193, 269, 273, 269, 1),  // fence - right rail base
                wall(50, 278, 95, 278, 3),    // lever alcove
                // bookshelves, left column
                wall(445, 128, 580, 128, 1),
                wall(444, 183, 580, 183, 1),
                wall(444, 238, 580, 238, 1),
                wall(444, 293, 580, 293, 1),
                // bookshelves, right column
                wall(667, 128, 732, 128, 1),
                wall(667, 183, 732, 183, 1),
                wall(667, 238, 732, 238, 1),
                wall(667, 293, 732, 293, 1));

        DoorSpec studyDoor = new DoorSpec(
                "study-door",
                wall(300, 154, 416, 154, 1),
                Rect.centeredAt(new Vec2(90, 292), 40, 40));

        GuardSpec corridorGoblin = new GuardSpec(
                "corridor-goblin",
                List.of(new Vec2(354, 80), new Vec2(635, 80)),
                19, 120, 30);
        GuardSpec carpetGoblin = new GuardSpec(
                "carpet-goblin",
                List.of(new Vec2(634, 70), new Vec2(634, 315)),
                16, 125, 50);

        return new LevelDefinition(
                1, "The Library", WORLD,
                new Vec2(100, 35),
                walls,
                List.of(studyDoor),
                List.of(corridorGoblin, carpetGoblin),
                new Rect(600, 300, 70, 53));
    }

    /** The maze. Work in progress in the original game: no guards yet. */
    private static LevelDefinition level2() {
        List<Rect> walls = List.of(
                wall(445, 0, 445, 120, 5),
                wall(446, 120, 580, 120, 5),
                wall(587, 115, 587, 175, 5),
                wall(593, 175, 667, 175, 5),
                wall(669, 178, 669, 65, 5),
                wall(667, 65, 505, 65, 3),
                wall(502, 75, 502, 15, 5),
                wall(504, 9, 775, 9, 5),
                wall(775, 15, 775, 67, 5),
                wall(775, 68, 744, 68, 5),
                wall(740, 75, 740, 360, 5),
                wall(738, 355, 493, 355, 5),
                wall(492, 363, 492, 300, 3),
                wall(493, 310, 660, 310, 5),
                wall(669, 310, 669, 240, 5),
                wall(667, 236, 590, 236, 5),
                wall(590, 236, 590, 260, 5),
                wall(585, 268, 458, 268, 5),
                wall(445, 266, 445, 390, 5),
                wall(374, 390, 374, 266, 5),
                wall(372, 266, 250, 266, 5),
                wall(248, 265, 248, 185, 5),
                wall(246, 186, 183, 186, 5),
                wall(178, 185, 178, 117, 5),
                wall(176, 118, 107, 118, 5),
                wall(103, 118, 103, 265, 5),
                wall(103, 265, 208, 265, 5),
                wall(215, 265, 215, 360, 5),
                wall(216, 360, 38, 360, 5),
                wall(30, 360, 30, 40, 5),
                wall(30, 35, 250, 35, 5),
                wall(250, 35, 250, 120, 5),
                wall(260, 130, 375, 130, 5),
                wall(373, 130, 373, 0, 5));

        return new LevelDefinition(
                2, "The Maze", WORLD,
                new Vec2(385, 5),
                walls,
                List.of(),
                List.of(),
                new Rect(378, 372, 64, 28));
    }

    private static Rect wall(double x1, double y1, double x2, double y2, double thickness) {
        return Rect.ofLine(x1, y1, x2, y2, thickness);
    }
}
