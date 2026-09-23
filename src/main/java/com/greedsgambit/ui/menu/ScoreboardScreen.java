package com.greedsgambit.ui.menu;

import com.greedsgambit.game.level.LevelDefinition;
import com.greedsgambit.game.level.Levels;
import com.greedsgambit.score.BestTime;
import com.greedsgambit.score.ScoreService;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import com.greedsgambit.util.TimeFormat;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.greedsgambit.ui.menu.MenuLayout.button;
import static com.greedsgambit.ui.menu.MenuLayout.label;

public final class ScoreboardScreen implements Screen {

    private static final int ENTRIES_PER_LEVEL = 5;

    private final Parent root;
    private final Button back;

    public ScoreboardScreen(AppRouter router, ScoreService scores) {
        HBox columns = new HBox(48);
        columns.setAlignment(Pos.TOP_CENTER);
        for (LevelDefinition level : Levels.all()) {
            columns.getChildren().add(levelColumn(level, scores.leaderboard(level.number(), ENTRIES_PER_LEVEL)));
        }

        back = button("Back", router::showMainMenu);
        root = MenuLayout.page("Scoreboard", columns, back);
        MenuLayout.onEscape(root, router::showMainMenu);
    }

    private static VBox levelColumn(LevelDefinition level, List<BestTime> entries) {
        VBox column = new VBox(8, label(level.number() + ". " + level.name(), "heading"));
        column.setAlignment(Pos.TOP_CENTER);
        if (entries.isEmpty()) {
            column.getChildren().add(label("No times yet", "muted"));
            return column;
        }
        GridPane table = new GridPane();
        table.setHgap(16);
        table.setVgap(4);
        for (int i = 0; i < entries.size(); i++) {
            BestTime entry = entries.get(i);
            table.addRow(i,
                    label((i + 1) + ".", "muted"),
                    new Label(entry.username()),
                    new Label(TimeFormat.minutesSeconds(entry.time())));
        }
        column.getChildren().add(table);
        return column;
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
