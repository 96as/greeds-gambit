package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;
import com.greedsgambit.game.level.DoorSpec;
import com.greedsgambit.game.level.GuardSpec;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.greedsgambit.game.TestLevels.DOWN;
import static com.greedsgambit.game.TestLevels.INTERACT;
import static com.greedsgambit.game.TestLevels.RIGHT;
import static com.greedsgambit.game.TestLevels.SPAWN;
import static com.greedsgambit.game.TestLevels.hold;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    /** A guard standing still (tiny route) at (200, 25), staring left along the top of the level. */
    private static GuardSpec sentryLookingLeft() {
        return new GuardSpec("sentry", List.of(new Vec2(200, 25), new Vec2(199.999, 25)), 0.0001, 200, 30);
    }

    @Nested
    class Doors {

        private final DoorSpec door = new DoorSpec("door", new Rect(100, 0, 5, 200), new Rect(0, 0, 60, 60));

        @Test
        void pullingTheLeverOpensAndClosesTheDoor() {
            GameSession session = new GameSession(TestLevels.withDoor(door));
            assertEquals(Optional.of(Interaction.PULL_LEVER), session.availableInteraction());
            assertTrue(session.solids().contains(door.wall()));

            List<GameEvent> events = session.update(0.01, INTERACT);

            assertEquals(List.of(new GameEvent.DoorToggled("door", true)), events);
            assertTrue(session.isDoorOpen("door"));
            assertFalse(session.solids().contains(door.wall()));

            session.update(0.01, INTERACT);
            assertFalse(session.isDoorOpen("door"));
        }

        @Test
        void aClosedDoorBlocksAndAnOpenOneDoesNot() {
            GameSession closed = new GameSession(TestLevels.withDoor(door));
            hold(closed, RIGHT, 2);
            assertEquals(door.wall().x(), closed.player().bounds().maxX(), 1e-9);

            GameSession open = new GameSession(TestLevels.withDoor(door));
            open.update(0.01, INTERACT);
            hold(open, RIGHT, 2);
            assertTrue(open.player().bounds().x() > door.wall().maxX());
        }

        @Test
        void leverDoesNothingFromAfar() {
            GameSession session = new GameSession(TestLevels.withDoor(door));
            hold(session, DOWN, 1);

            assertEquals(Optional.empty(), session.availableInteraction());
            assertEquals(List.of(), session.update(0.01, INTERACT));
        }
    }

    @Nested
    class Exit {

        private final Rect exit = new Rect(0, 0, 60, 60);

        @Test
        void interactingInTheExitZoneCompletesTheLevel() {
            GameSession session = new GameSession(TestLevels.level(List.of(), List.of(), List.of(), exit));
            hold(session, InputSnapshot.NONE, 2);
            assertEquals(Optional.of(Interaction.EXIT_LEVEL), session.availableInteraction());

            List<GameEvent> events = session.update(0.5, INTERACT);

            assertEquals(SessionStatus.LEVEL_COMPLETE, session.status());
            assertEquals(1, events.size());
            GameEvent.LevelCompleted completed = assertInstanceOf(GameEvent.LevelCompleted.class, events.get(0));
            assertEquals(2.5, completed.elapsedSeconds(), 1e-9);
        }

        @Test
        void standingInTheExitZoneIsNotEnough() {
            GameSession session = new GameSession(TestLevels.level(List.of(), List.of(), List.of(), exit));

            hold(session, InputSnapshot.NONE, 1);

            assertEquals(SessionStatus.PLAYING, session.status());
        }

        @Test
        void nothingChangesAfterTheLevelIsOver() {
            GameSession session = new GameSession(TestLevels.level(List.of(), List.of(), List.of(), exit));
            session.update(0.1, INTERACT);
            Rect finishedAt = session.player().bounds();

            assertEquals(List.of(), session.update(1, RIGHT));
            assertEquals(finishedAt, session.player().bounds());
        }
    }

    @Nested
    class Guards {

        @Test
        void beingSeenCostsALifeAndSendsThePlayerBackToTheStart() {
            GameSession session = new GameSession(TestLevels.withGuards(List.of(), sentryLookingLeft()));

            List<GameEvent> events = session.update(0.01, InputSnapshot.NONE);

            assertEquals(List.of(new GameEvent.PlayerCaught("sentry", 2)), events);
            assertEquals(2, session.lives());
            assertEquals(SPAWN, session.player().bounds().topLeft());
            assertTrue(session.isInvulnerable());
        }

        @Test
        void playerCannotBeCaughtAgainDuringTheGracePeriod() {
            GameSession session = new GameSession(TestLevels.withGuards(List.of(), sentryLookingLeft()));
            session.update(0.01, InputSnapshot.NONE);

            hold(session, InputSnapshot.NONE, GameSession.RESPAWN_GRACE_SECONDS - 0.1);
            assertEquals(2, session.lives());

            hold(session, InputSnapshot.NONE, 0.2);
            assertEquals(1, session.lives());
        }

        @Test
        void losingTheLastLifeEndsTheGame() {
            GameSession session = new GameSession(TestLevels.withGuards(List.of(), sentryLookingLeft()));

            hold(session, InputSnapshot.NONE, GameSession.RESPAWN_GRACE_SECONDS * 3);

            assertEquals(0, session.lives());
            assertEquals(SessionStatus.GAME_OVER, session.status());
        }

        @Test
        void guardsCannotSeeThroughWalls() {
            Rect wallBetween = new Rect(100, 0, 5, 200);
            GameSession session = new GameSession(TestLevels.withGuards(List.of(wallBetween), sentryLookingLeft()));

            hold(session, InputSnapshot.NONE, 1);

            assertEquals(GameSession.STARTING_LIVES, session.lives());
        }

        @Test
        void walkingIntoAGuardGetsThePlayerCaught() {
            GuardSpec lookingAway = new GuardSpec("g", List.of(new Vec2(80, 28), new Vec2(80.001, 28)), 0.0001, 10, 10);
            GameSession session = new GameSession(TestLevels.withGuards(List.of(), lookingAway));

            List<GameEvent> events = session.update(0.2, RIGHT);

            assertEquals(List.of(new GameEvent.PlayerCaught("g", 2)), events);
        }
    }
}
