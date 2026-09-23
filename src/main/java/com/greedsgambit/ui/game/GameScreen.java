package com.greedsgambit.ui.game;

import com.greedsgambit.account.Account;
import com.greedsgambit.game.GameEvent;
import com.greedsgambit.game.GameSession;
import com.greedsgambit.game.level.LevelDefinition;
import com.greedsgambit.game.level.Levels;
import com.greedsgambit.score.ScoreService;
import com.greedsgambit.storage.StorageException;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Assets;
import com.greedsgambit.ui.Screen;
import com.greedsgambit.ui.Settings;
import com.greedsgambit.util.TimeFormat;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Plays one level: owns the {@link GameSession}, feeds it keyboard input from a fixed-step loop,
 * reacts to its events and shows the pause / game-over / level-complete overlays.
 */
public final class GameScreen implements Screen {

    private static final System.Logger LOG = System.getLogger(GameScreen.class.getName());

    private final AppRouter router;
    private final Account account;
    private final ScoreService scores;
    private final GameSession session;
    private final WorldView view;
    private final KeyboardInput input = new KeyboardInput();
    private final FixedStepLoop loop;

    private final StackPane root = new StackPane();
    private final StackPane overlayLayer = new StackPane();
    private final ChangeListener<Boolean> pauseWhenUnfocused = (observable, was, focused) -> {
        if (!focused) {
            pause();
        }
    };
    private boolean paused;

    public GameScreen(AppRouter router, Account account, LevelDefinition level,
                      ScoreService scores, Settings settings, Assets assets) {
        this.router = router;
        this.account = account;
        this.scores = scores;
        this.session = new GameSession(level);
        this.view = new WorldView(session, assets, settings.showDebugOverlayProperty());
        this.loop = new FixedStepLoop(this::step, view::render);

        Group world = new Group(view.node());
        DoubleBinding scale = Bindings.createDoubleBinding(
                () -> Math.min(root.getWidth() / level.bounds().width(), root.getHeight() / level.bounds().height()),
                root.widthProperty(), root.heightProperty());
        world.scaleXProperty().bind(scale);
        world.scaleYProperty().bind(scale);

        overlayLayer.setPickOnBounds(false);
        root.getChildren().addAll(world, overlayLayer);
        root.getStyleClass().add("game-root");
        root.setFocusTraversable(true);

        input.attachTo(root);
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        root.requestFocus();
        window().focusedProperty().addListener(pauseWhenUnfocused);
        loop.start();
    }

    @Override
    public void onHide() {
        loop.stop();
        window().focusedProperty().removeListener(pauseWhenUnfocused);
    }

    private void step(double dt) {
        for (GameEvent event : session.update(dt, input.poll())) {
            switch (event) {
                case GameEvent.DoorToggled toggled -> view.refreshStaticLayers();
                case GameEvent.PlayerCaught caught -> LOG.log(System.Logger.Level.DEBUG, "Caught by {0}", caught.guardId());
                case GameEvent.GameOver over -> finish(this::showGameOver);
                case GameEvent.LevelCompleted completed -> finish(() -> showLevelComplete(completed));
            }
        }
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE || session.status().isFinished()) {
            return;
        }
        event.consume();
        if (paused) {
            resume();
        } else {
            pause();
        }
    }

    private void pause() {
        if (paused || session.status().isFinished()) {
            return;
        }
        paused = true;
        loop.stop();
        input.clear();
        showOverlay("Paused", null,
                button("Resume", this::resume),
                button("Restart", this::restart),
                button("Main menu", router::showMainMenu));
    }

    private void resume() {
        paused = false;
        overlayLayer.getChildren().clear();
        input.clear();
        root.requestFocus();
        loop.start();
    }

    private void finish(Runnable showResult) {
        loop.stop();
        input.clear();
        view.render();
        showResult.run();
    }

    private void showGameOver() {
        showOverlay("Caught!", "The goblins got you three times.",
                button("Try again", this::restart),
                button("Main menu", router::showMainMenu));
    }

    private void showLevelComplete(GameEvent.LevelCompleted completed) {
        Duration time = TimeFormat.ofSeconds(completed.elapsedSeconds());
        String message = "Time " + TimeFormat.minutesSeconds(time) + saveBestTime(completed.levelNumber(), time);

        List<Button> actions = new ArrayList<>();
        int next = completed.levelNumber() + 1;
        if (Levels.exists(next)) {
            actions.add(button("Next level", () -> router.startLevel(next)));
        }
        actions.add(button("Play again", this::restart));
        actions.add(button("Main menu", router::showMainMenu));
        showOverlay("You escaped!", message, actions.toArray(Button[]::new));
    }

    private String saveBestTime(int levelNumber, Duration time) {
        try {
            return scores.recordCompletion(account, levelNumber, time) ? "  -  new best!" : "";
        } catch (StorageException e) {
            LOG.log(System.Logger.Level.WARNING, "Could not save best time", e);
            return "  (could not save your time)";
        }
    }

    private void restart() {
        router.startLevel(session.level().number());
    }

    private void showOverlay(String title, String message, Button... actions) {
        Label heading = new Label(title);
        heading.getStyleClass().add("title");
        VBox box = new VBox(14, heading);
        if (message != null) {
            box.getChildren().add(new Label(message));
        }
        box.getChildren().addAll(actions);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("overlay");

        overlayLayer.getChildren().setAll(box);
        actions[0].requestFocus();
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    private Window window() {
        return root.getScene().getWindow();
    }
}
