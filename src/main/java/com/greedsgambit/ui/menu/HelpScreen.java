package com.greedsgambit.ui.menu;

import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import static com.greedsgambit.ui.menu.MenuLayout.button;
import static com.greedsgambit.ui.menu.MenuLayout.label;

public final class HelpScreen implements Screen {

    private static final String[][] CONTROLS = {
            {"W A S D / Arrows", "Move"},
            {"Shift", "Run"},
            {"E", "Pull a lever / take the exit"},
            {"Esc", "Pause"},
    };

    private final Parent root;
    private final Button back;

    public HelpScreen(AppRouter router) {
        GridPane keys = new GridPane();
        keys.setHgap(24);
        keys.setVgap(10);
        for (int row = 0; row < CONTROLS.length; row++) {
            keys.addRow(row, label(CONTROLS[row][0], "key-cap"), new Label(CONTROLS[row][1]));
        }

        Label goal = label("Pull the lever to open the door, stay out of the goblins' sight"
                + " and reach the stairs. Three strikes and you're out.", "muted");
        goal.setWrapText(true);
        goal.setMaxWidth(520);

        back = button("Back", router::showMainMenu);
        root = MenuLayout.page("How to play", keys, goal, back);
        MenuLayout.onEscape(root, router::showMainMenu);
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        back.requestFocus();
    }
}
