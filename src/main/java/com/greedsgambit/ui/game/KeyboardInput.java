package com.greedsgambit.ui.game;

import com.greedsgambit.game.InputSnapshot;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Turns JavaFX key events into {@link InputSnapshot}s. Movement uses keys that are held down, so
 * speed no longer depends on the operating system's key-repeat rate; interacting is a single press.
 */
final class KeyboardInput {

    private final Set<KeyCode> held = EnumSet.noneOf(KeyCode.class);
    private boolean interactPressed;

    void attachTo(Node node) {
        node.addEventHandler(KeyEvent.KEY_PRESSED, this::onPressed);
        node.addEventHandler(KeyEvent.KEY_RELEASED, event -> held.remove(event.getCode()));
    }

    private void onPressed(KeyEvent event) {
        boolean firstPress = held.add(event.getCode());
        if (firstPress && event.getCode() == KeyCode.E) {
            interactPressed = true;
        }
    }

    /** The input for the next simulation step. Consumes a pending interact press. */
    InputSnapshot poll() {
        InputSnapshot snapshot = new InputSnapshot(
                isHeld(KeyCode.W, KeyCode.UP),
                isHeld(KeyCode.S, KeyCode.DOWN),
                isHeld(KeyCode.A, KeyCode.LEFT),
                isHeld(KeyCode.D, KeyCode.RIGHT),
                isHeld(KeyCode.SHIFT),
                interactPressed);
        interactPressed = false;
        return snapshot;
    }

    /** Forget every key, e.g. when the window loses focus and key-release events would be missed. */
    void clear() {
        held.clear();
        interactPressed = false;
    }

    private boolean isHeld(KeyCode... keys) {
        for (KeyCode key : keys) {
            if (held.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
