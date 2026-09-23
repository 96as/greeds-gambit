package com.greedsgambit.ui.menu;

import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import com.greedsgambit.ui.Settings;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import static com.greedsgambit.ui.menu.MenuLayout.button;

public final class SettingsScreen implements Screen {

    private final Parent root;
    private final Button back;

    public SettingsScreen(AppRouter router, Settings settings) {
        CheckBox debugOverlay = new CheckBox("Show walls and trigger zones");
        debugOverlay.selectedProperty().bindBidirectional(settings.showDebugOverlayProperty());

        back = button("Back", router::showMainMenu);
        root = MenuLayout.page("Settings", debugOverlay, back);
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
