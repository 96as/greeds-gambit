package com.greedsgambit;

import com.greedsgambit.account.AccountService;
import com.greedsgambit.account.FileCredentialStore;
import com.greedsgambit.account.PasswordHasher;
import com.greedsgambit.score.FileScoreStore;
import com.greedsgambit.score.ScoreService;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Assets;
import com.greedsgambit.ui.Settings;
import com.greedsgambit.ui.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point and composition root: the only place that creates services and decides where data
 * is saved. Everything else receives what it needs through its constructor.
 */
public final class GreedsGambitApp extends Application {

    /** Overrides where accounts and scores are saved (default: {@code ~/.greeds-gambit}). */
    public static final String DATA_DIR_PROPERTY = "greedsgambit.home";

    private static final double WINDOW_WIDTH = 1125;
    private static final double WINDOW_HEIGHT = 640;

    private final ExecutorService background = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "greeds-gambit-background");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void start(Stage stage) {
        Path dataDir = dataDirectory();
        AccountService accounts = new AccountService(
                new FileCredentialStore(dataDir.resolve("accounts.properties")), new PasswordHasher());
        ScoreService scores = new ScoreService(new FileScoreStore(dataDir.resolve("scores.properties")));

        Theme.loadFonts();
        Scene scene = new Scene(new Pane(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Theme.stylesheet());

        AppRouter router = new AppRouter(scene, accounts, scores, new Settings(), new Assets(), background);

        stage.setTitle("Greed's Gambit");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(480);
        router.showWelcome();
        stage.show();
    }

    @Override
    public void stop() {
        background.shutdownNow();
    }

    private static Path dataDirectory() {
        String override = System.getProperty(DATA_DIR_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.home"), ".greeds-gambit");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
