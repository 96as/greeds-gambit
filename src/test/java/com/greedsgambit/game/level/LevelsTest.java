package com.greedsgambit.game.level;

import com.greedsgambit.game.Player;
import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Validates the hand-traced level data, so a typo in a coordinate fails the build instead of
 * producing a level that cannot be finished.
 */
class LevelsTest {

    @Test
    void levelsAreNumberedFromOneWithoutGaps() {
        List<Integer> numbers = Levels.all().stream().map(LevelDefinition::number).toList();

        assertEquals(IntStream.rangeClosed(1, numbers.size()).boxed().toList(), numbers);
    }

    @TestFactory
    Stream<DynamicTest> everyLevelIsWellFormed() {
        return Levels.all().stream().flatMap(level -> Stream.of(
                dynamicTest(level.name() + ": player starts inside the level and not inside a wall", () -> {
                    Rect start = playerAt(level.playerSpawn());
                    assertTrue(inside(start, level.bounds()));
                    assertTrue(solids(level, false).stream().noneMatch(start::intersects));
                }),
                dynamicTest(level.name() + ": exit and guard routes are inside the level", () -> {
                    assertTrue(inside(level.exitZone(), level.bounds()));
                    level.guards().forEach(guard -> guard.route().forEach(point ->
                            assertTrue(level.bounds().contains(point), guard.id() + " leaves the level at " + point)));
                }),
                dynamicTest(level.name() + ": every lever can be reached", () ->
                        level.doors().forEach(door ->
                                assertTrue(reachable(level, false, door.leverZone()), door.id() + " lever"))),
                dynamicTest(level.name() + ": the exit can be reached once the doors are open", () ->
                        assertTrue(reachable(level, true, level.exitZone())))));
    }

    @Test
    void theLibraryExitIsLockedUntilTheLeverIsPulled() {
        LevelDefinition library = Levels.byNumber(1);

        assertFalse(reachable(library, false, library.exitZone()));
    }

    /** Flood-fills every position the player can walk to, one unit at a time. */
    private static boolean reachable(LevelDefinition level, boolean doorsOpen, Rect target) {
        List<Rect> solids = solids(level, doorsOpen);
        Rect bounds = level.bounds();
        int originX = (int) bounds.x();
        int originY = (int) bounds.y();
        int columns = (int) (bounds.width() - Player.WIDTH) + 1;
        int rows = (int) (bounds.height() - Player.HEIGHT) + 1;
        boolean[][] visited = new boolean[columns][rows];

        Deque<int[]> queue = new ArrayDeque<>();
        int startX = (int) Math.round(level.playerSpawn().x()) - originX;
        int startY = (int) Math.round(level.playerSpawn().y()) - originY;
        queue.add(new int[] {startX, startY});
        visited[startX][startY] = true;

        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            Rect body = playerAt(new Vec2(originX + cell[0], originY + cell[1]));
            if (body.intersects(target)) {
                return true;
            }
            for (int[] move : moves) {
                int x = cell[0] + move[0];
                int y = cell[1] + move[1];
                if (x < 0 || y < 0 || x >= columns || y >= rows || visited[x][y]) {
                    continue;
                }
                visited[x][y] = true;
                Rect next = playerAt(new Vec2(originX + x, originY + y));
                if (solids.stream().noneMatch(next::intersects)) {
                    queue.add(new int[] {x, y});
                }
            }
        }
        return false;
    }

    private static List<Rect> solids(LevelDefinition level, boolean doorsOpen) {
        List<Rect> solids = new ArrayList<>(level.walls());
        if (!doorsOpen) {
            level.doors().forEach(door -> solids.add(door.wall()));
        }
        return solids;
    }

    private static Rect playerAt(Vec2 topLeft) {
        return new Rect(topLeft.x(), topLeft.y(), Player.WIDTH, Player.HEIGHT);
    }

    private static boolean inside(Rect inner, Rect outer) {
        return inner.x() >= outer.x() && inner.y() >= outer.y()
                && inner.maxX() <= outer.maxX() && inner.maxY() <= outer.maxY();
    }
}
