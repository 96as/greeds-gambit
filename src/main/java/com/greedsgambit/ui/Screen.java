package com.greedsgambit.ui;

import javafx.scene.Parent;

/** One full-window page of the app. {@link AppRouter} shows one screen at a time. */
public interface Screen {

    Parent root();

    /** Called after the screen becomes visible. */
    default void onShow() {
    }

    /** Called before the screen is replaced; release timers and listeners here. */
    default void onHide() {
    }
}
