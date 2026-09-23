package com.greedsgambit.ui;

import com.greedsgambit.account.Account;
import com.greedsgambit.account.AccountService;
import com.greedsgambit.game.level.Levels;
import com.greedsgambit.score.ScoreService;
import com.greedsgambit.ui.game.GameScreen;
import com.greedsgambit.ui.menu.HelpScreen;
import com.greedsgambit.ui.menu.LevelSelectScreen;
import com.greedsgambit.ui.menu.LoginScreen;
import com.greedsgambit.ui.menu.MainMenuScreen;
import com.greedsgambit.ui.menu.ScoreboardScreen;
import com.greedsgambit.ui.menu.SettingsScreen;
import com.greedsgambit.ui.menu.SignUpScreen;
import com.greedsgambit.ui.menu.WelcomeScreen;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.util.concurrent.Executor;

/**
 * Moves between screens and owns who is signed in. There is a single {@link Scene}; each screen
 * swaps in its own root node, so the window never flickers or changes size between pages.
 */
public final class AppRouter {

    private final Scene scene;
    private final AccountService accounts;
    private final ScoreService scores;
    private final Settings settings;
    private final Assets assets;
    private final Executor background;

    private Screen current;
    private Account account;

    public AppRouter(Scene scene, AccountService accounts, ScoreService scores,
                     Settings settings, Assets assets, Executor background) {
        this.scene = scene;
        this.accounts = accounts;
        this.scores = scores;
        this.settings = settings;
        this.assets = assets;
        this.background = background;
    }

    public void showWelcome() {
        account = null;
        show(new WelcomeScreen(this));
    }

    public void showLogin() {
        show(new LoginScreen(this, accounts, background));
    }

    public void showSignUp() {
        show(new SignUpScreen(this, accounts, background));
    }

    public void signedIn(Account signedIn) {
        account = signedIn;
        showMainMenu();
    }

    public void showMainMenu() {
        show(new MainMenuScreen(this, requireAccount()));
    }

    public void showLevelSelect() {
        show(new LevelSelectScreen(this, requireAccount(), scores));
    }

    public void showScoreboard() {
        show(new ScoreboardScreen(this, scores));
    }

    public void showSettings() {
        show(new SettingsScreen(this, settings));
    }

    public void showHelp() {
        show(new HelpScreen(this));
    }

    public void startLevel(int levelNumber) {
        show(new GameScreen(this, requireAccount(), Levels.byNumber(levelNumber), scores, settings, assets));
    }

    public void exit() {
        Platform.exit();
    }

    private void show(Screen next) {
        if (current != null) {
            current.onHide();
        }
        current = next;
        scene.setRoot(next.root());
        next.onShow();
    }

    private Account requireAccount() {
        if (account == null) {
            throw new IllegalStateException("No player is signed in");
        }
        return account;
    }
}
