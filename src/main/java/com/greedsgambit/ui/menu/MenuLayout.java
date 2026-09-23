package com.greedsgambit.ui.menu;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Building blocks shared by every menu page, so they all look and behave the same. */
final class MenuLayout {

    private MenuLayout() {
    }

    /** A titled panel centred over the animated menu background. */
    static StackPane page(String title, Node... content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("title");

        VBox panel = new VBox(14, heading);
        panel.getChildren().addAll(content);
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("menu-panel");
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane root = new StackPane(panel);
        root.getStyleClass().add("menu-root");
        return root;
    }

    static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    /** Runs {@code action} when Escape is pressed anywhere on the page. */
    static void onEscape(Node root, Runnable action) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                action.run();
            }
        });
    }
}
