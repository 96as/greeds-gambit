package com.greedsgambit.game;

/** Notable things that happened during a simulation step, for the UI and score keeping. */
public sealed interface GameEvent {

    record PlayerCaught(String guardId, int livesLeft) implements GameEvent {
    }

    record DoorToggled(String doorId, boolean open) implements GameEvent {
    }

    record LevelCompleted(int levelNumber, double elapsedSeconds) implements GameEvent {
    }

    record GameOver(int levelNumber) implements GameEvent {
    }
}
