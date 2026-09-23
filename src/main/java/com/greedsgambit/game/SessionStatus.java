package com.greedsgambit.game;

public enum SessionStatus {
    PLAYING, LEVEL_COMPLETE, GAME_OVER;

    public boolean isFinished() {
        return this != PLAYING;
    }
}
