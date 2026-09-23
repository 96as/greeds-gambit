package com.greedsgambit.ui.menu;

import com.greedsgambit.account.Account;
import com.greedsgambit.game.level.LevelDefinition;
import com.greedsgambit.game.level.Levels;
import com.greedsgambit.score.ScoreService;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import com.greedsgambit.util.TimeFormat;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

import static com.greedsgambit.ui.menu.MenuLayout.button;
import static com.greedsgambit.ui.menu.MenuLayout.label;

public final class LevelSelectScreen implements Screen {

    private final Parent root;
    private final List<Button> levelButtons = new ArrayList<>();

    public LevelSelectScreen(AppRouter router, Account account, ScoreService scores) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        int row = 0;
        for (LevelDefinition level : Levels.all()) {
            int number = level.number();
            boolean unlocked = scores.isUnlocked(account, number);

            Button start = button(number + ". " + level.name(), () -> router.startLevel(number));
            start.setDisable(!unlocked);
            levelButtons.add(start);

            String status = !unlocked ? "Locked"
                    : scores.bestTime(account, number).map(time -> "Best " + TimeFormat.minutesSeconds(time))
                    .orElse("Not finished yet");
            Label statusLabel = label(status, "muted");

            grid.addRow(row++, start, statusLabel);
        }

        root = MenuLayout.page("Levels", grid, button("Back", router::showMainMenu));
        MenuLayout.onEscape(root, router::showMainMenu);
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        levelButtons.stream().filter(button -> !button.isDisabled()).reduce((first, second) -> second)
                .ifPresent(Button::requestFocus);
    }
}
