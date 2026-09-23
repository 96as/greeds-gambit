package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.level.DoorSpec;
import com.greedsgambit.game.level.LevelDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * One attempt at one level: the complete, UI-independent game state and rules.
 *
 * <p>The UI calls {@link #update} with a fixed time step and then reads the state back to draw it.
 * Nothing here knows about JavaFX, so every rule can be unit-tested with plain inputs.
 */
public final class GameSession {

    public static final int STARTING_LIVES = 3;
    /** After being caught the player cannot be caught again for this long. */
    public static final double RESPAWN_GRACE_SECONDS = 1.5;

    private final LevelDefinition level;
    private final Player player;
    private final List<Guard> guards;
    private final Set<String> openDoors = new HashSet<>();

    private SessionStatus status = SessionStatus.PLAYING;
    private int lives = STARTING_LIVES;
    private double elapsedSeconds;
    private double graceSecondsLeft;

    public GameSession(LevelDefinition level) {
        this.level = level;
        this.player = new Player(level.playerSpawn());
        this.guards = level.guards().stream().map(Guard::new).toList();
    }

    /** Advances the simulation by {@code dt} seconds. Returns what happened during the step. */
    public List<GameEvent> update(double dt, InputSnapshot input) {
        if (status.isFinished()) {
            return List.of();
        }
        List<GameEvent> events = new ArrayList<>();
        elapsedSeconds += dt;
        graceSecondsLeft = Math.max(0, graceSecondsLeft - dt);

        List<Rect> solids = solids();
        player.move(input, dt, solids, level.bounds());
        guards.forEach(guard -> guard.update(dt));

        if (input.interact()) {
            interact(events);
        }
        if (status == SessionStatus.PLAYING && graceSecondsLeft == 0) {
            checkSpotted(events);
        }
        return List.copyOf(events);
    }

    /** What pressing the interact key would do right now, if anything - used for on-screen hints. */
    public Optional<Interaction> availableInteraction() {
        if (leverInReach().isPresent()) {
            return Optional.of(Interaction.PULL_LEVER);
        }
        if (player.bounds().intersects(level.exitZone())) {
            return Optional.of(Interaction.EXIT_LEVEL);
        }
        return Optional.empty();
    }

    private void interact(List<GameEvent> events) {
        Optional<DoorSpec> lever = leverInReach();
        if (lever.isPresent()) {
            String doorId = lever.get().id();
            boolean nowOpen = !openDoors.contains(doorId);
            if (nowOpen) {
                openDoors.add(doorId);
            } else {
                openDoors.remove(doorId);
            }
            events.add(new GameEvent.DoorToggled(doorId, nowOpen));
        } else if (player.bounds().intersects(level.exitZone())) {
            status = SessionStatus.LEVEL_COMPLETE;
            events.add(new GameEvent.LevelCompleted(level.number(), elapsedSeconds));
        }
    }

    private Optional<DoorSpec> leverInReach() {
        return level.doors().stream()
                .filter(door -> player.bounds().intersects(door.leverZone()))
                .findFirst();
    }

    private void checkSpotted(List<GameEvent> events) {
        Rect body = player.bounds();
        List<Rect> occluders = solids();
        for (Guard guard : guards) {
            if (guard.bounds().intersects(body) || guard.vision().sees(body, occluders)) {
                caught(guard, events);
                return;
            }
        }
    }

    private void caught(Guard guard, List<GameEvent> events) {
        lives--;
        events.add(new GameEvent.PlayerCaught(guard.id(), lives));
        if (lives == 0) {
            status = SessionStatus.GAME_OVER;
            events.add(new GameEvent.GameOver(level.number()));
        } else {
            player.teleport(level.playerSpawn());
            graceSecondsLeft = RESPAWN_GRACE_SECONDS;
        }
    }

    /** Walls plus every door that is currently closed. */
    public List<Rect> solids() {
        List<Rect> solids = new ArrayList<>(level.walls());
        for (DoorSpec door : level.doors()) {
            if (!openDoors.contains(door.id())) {
                solids.add(door.wall());
            }
        }
        return solids;
    }

    public LevelDefinition level() {
        return level;
    }

    public Player player() {
        return player;
    }

    public List<Guard> guards() {
        return guards;
    }

    public boolean isDoorOpen(String doorId) {
        return openDoors.contains(doorId);
    }

    public SessionStatus status() {
        return status;
    }

    public int lives() {
        return lives;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isInvulnerable() {
        return graceSecondsLeft > 0;
    }
}
