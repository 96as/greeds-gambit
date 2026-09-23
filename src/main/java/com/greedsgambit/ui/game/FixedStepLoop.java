package com.greedsgambit.ui.game;

import javafx.animation.AnimationTimer;

import java.util.function.DoubleConsumer;

/**
 * Runs the simulation at a fixed rate regardless of the display's frame rate, then renders once
 * per frame. A fixed step keeps movement and collisions identical on 60 Hz and 144 Hz screens.
 */
final class FixedStepLoop extends AnimationTimer {

    static final double STEP_SECONDS = 1.0 / 120;
    /** Caps catch-up after a stall (e.g. window dragged) so the game does not fast-forward. */
    private static final double MAX_FRAME_SECONDS = 0.25;

    private final DoubleConsumer step;
    private final Runnable render;
    private long lastNanos = -1;
    private double accumulator;

    FixedStepLoop(DoubleConsumer step, Runnable render) {
        this.step = step;
        this.render = render;
    }

    @Override
    public void start() {
        lastNanos = -1;
        accumulator = 0;
        super.start();
    }

    @Override
    public void handle(long now) {
        if (lastNanos < 0) {
            lastNanos = now;
            render.run();
            return;
        }
        accumulator += Math.min((now - lastNanos) / 1e9, MAX_FRAME_SECONDS);
        lastNanos = now;
        while (accumulator >= STEP_SECONDS) {
            step.accept(STEP_SECONDS);
            accumulator -= STEP_SECONDS;
        }
        render.run();
    }
}
