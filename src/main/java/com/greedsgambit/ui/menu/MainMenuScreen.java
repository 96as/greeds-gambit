package com.greedsgambit.ui.menu;

import com.greedsgambit.account.Account;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import javafx.scene.Parent;
import javafx.scene.control.Button;

import static com.greedsgambit.ui.menu.MenuLayout.button;
import static com.greedsgambit.ui.menu.MenuLayout.label;

public final class MainMenuScreen implements Screen {

    private final Parent root;
    private final Button play;

    public MainMenuScreen(AppRouter router, Account account) {
        play = button("Play", router::showLevelSelect);
        root = MenuLayout.page("Greed's Gambit",
                label("Welcome, " + account.username(), "muted"),
                play,
                button("Scoreboard", router::showScoreboard),
                button("Settings", router::showSettings),
                button("Help", router::showHelp),
                button("Log out", router::showWelcome),
                button("Exit", router::exit));
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        play.requestFocus();
    }
}
