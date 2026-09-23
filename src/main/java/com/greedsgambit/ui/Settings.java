package com.greedsgambit.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/** Player preferences for this run of the game. */
public final class Settings {

    private final BooleanProperty showDebugOverlay = new SimpleBooleanProperty(false);

    /** Draws walls, lever and exit zones - handy when editing level data. */
    public BooleanProperty showDebugOverlayProperty() {
        return showDebugOverlay;
    }
}
