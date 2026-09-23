package com.greedsgambit.ui;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Loads images from the application's resources once and shares them. */
public final class Assets {

    private static final String IMAGE_ROOT = "/com/greedsgambit/images/";

    private final Map<String, Image> cache = new HashMap<>();

    /** @param path relative to the images folder, e.g. {@code "player/idle-right.gif"} */
    public Image image(String path) {
        return cache.computeIfAbsent(path, key -> new Image(resource(IMAGE_ROOT + key).toExternalForm()));
    }

    static URL resource(String absolutePath) {
        URL url = Assets.class.getResource(absolutePath);
        if (url == null) {
            throw new IllegalStateException("Missing resource: " + absolutePath);
        }
        return url;
    }
}
