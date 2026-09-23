package com.greedsgambit.ui;

import javafx.scene.text.Font;

import java.util.List;

/** The shared look of every screen: fonts and the stylesheet. */
public final class Theme {

    private static final System.Logger LOG = System.getLogger(Theme.class.getName());
    private static final List<String> FONTS = List.of(
            "/com/greedsgambit/fonts/connection-regular.otf",
            "/com/greedsgambit/fonts/connection-bold.otf");

    private Theme() {
    }

    /** Must run before any stylesheet that refers to the "Connection" font family is applied. */
    public static void loadFonts() {
        for (String font : FONTS) {
            if (Font.loadFont(Assets.resource(font).toExternalForm(), 20) == null) {
                LOG.log(System.Logger.Level.WARNING, "Could not load font {0}; using the system font", font);
            }
        }
    }

    public static String stylesheet() {
        return Assets.resource("/com/greedsgambit/css/theme.css").toExternalForm();
    }
}
