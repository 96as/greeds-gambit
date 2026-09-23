package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import org.junit.jupiter.api.Test;

import static com.greedsgambit.game.TestLevels.DOWN_RIGHT;
import static com.greedsgambit.game.TestLevels.LEFT;
import static com.greedsgambit.game.TestLevels.RIGHT;
import static com.greedsgambit.game.TestLevels.RUN_RIGHT;
import static com.greedsgambit.game.TestLevels.hold;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerMovementTest {

    @Test
    void walksAtWalkingSpeedAndFacesTheWayItMoves() {
        GameSession session = new GameSession(TestLevels.empty());

        hold(session, RIGHT, 1);

        Player player = session.player();
        assertEquals(10 + Player.WALK_SPEED, player.bounds().x(), 1e-6);
        assertEquals(Motion.WALKING, player.motion());
        assertEquals(Facing.RIGHT, player.facing());

        hold(session, LEFT, 0.1);
        assertEquals(Facing.LEFT, player.facing());
    }

    @Test
    void runsFasterThanItWalks() {
        GameSession session = new GameSession(TestLevels.empty());

        hold(session, RUN_RIGHT, 1);

        assertEquals(10 + Player.RUN_SPEED, session.player().bounds().x(), 1e-6);
        assertEquals(Motion.RUNNING, session.player().motion());
    }

    @Test
    void diagonalMovementIsNotFaster() {
        GameSession session = new GameSession(TestLevels.empty());

        hold(session, DOWN_RIGHT, 1);

        Rect bounds = session.player().bounds();
        double travelled = Math.hypot(bounds.x() - 10, bounds.y() - 10);
        assertEquals(Player.WALK_SPEED, travelled, 1e-6);
    }

    @Test
    void stopsFlushAgainstAWall() {
        Rect wall = new Rect(100, 0, 5, 200);
        GameSession session = new GameSession(TestLevels.withWalls(wall));

        hold(session, RIGHT, 3);

        assertEquals(wall.x(), session.player().bounds().maxX(), 1e-9);
    }

    @Test
    void slidesAlongAWallWhenPushingDiagonallyIntoIt() {
        Rect wall = new Rect(100, 0, 5, 200);
        GameSession session = new GameSession(TestLevels.withWalls(wall));

        hold(session, DOWN_RIGHT, 2);

        assertEquals(wall.x(), session.player().bounds().maxX(), 1e-9);
        assertEquals(10 + Player.WALK_SPEED * 2 / Math.sqrt(2), session.player().bounds().y(), 1e-6);
    }

    @Test
    void cannotLeaveTheLevel() {
        GameSession session = new GameSession(TestLevels.empty());

        hold(session, LEFT, 2);
        hold(session, RUN_RIGHT, 5);

        assertEquals(TestLevels.BOUNDS.maxX(), session.player().bounds().maxX(), 1e-9);
    }

    @Test
    void idlesWithoutInput() {
        GameSession session = new GameSession(TestLevels.empty());

        hold(session, RIGHT, 0.5);
        hold(session, InputSnapshot.NONE, 0.1);

        assertEquals(Motion.IDLE, session.player().motion());
    }
}
