package com.greedsgambit.ui.menu;

import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import javafx.scene.Parent;
import javafx.scene.control.Button;

import static com.greedsgambit.ui.menu.MenuLayout.button;

public final class WelcomeScreen implements Screen {

    private final Parent root;
    private final Button logIn;

    public WelcomeScreen(AppRouter router) {
        logIn = button("Log in", router::showLogin);
        root = MenuLayout.page("Greed's Gambit",
                logIn,
                button("Sign up", router::showSignUp),
                button("Exit", router::exit));
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        logIn.requestFocus();
    }
}
