package com.greedsgambit.ui.game;

import com.greedsgambit.game.Direction;
import com.greedsgambit.game.Facing;
import com.greedsgambit.game.GameSession;
import com.greedsgambit.game.Guard;
import com.greedsgambit.game.Interaction;
import com.greedsgambit.game.Motion;
import com.greedsgambit.game.Player;
import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.VisionCone;
import com.greedsgambit.game.level.DoorSpec;
import com.greedsgambit.game.level.LevelDefinition;
import com.greedsgambit.ui.Assets;
import com.greedsgambit.util.TimeFormat;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Draws a {@link GameSession} in world coordinates. It only reads the session; all rules live in
 * the model, so this class is free to change how things look without changing how the game plays.
 */
final class WorldView {

    private static final Map<Direction, String> GOBLIN_SPRITES = Map.of(
            Direction.UP, "goblin/up.gif",
            Direction.DOWN, "goblin/down.gif",
            Direction.LEFT, "goblin/left.gif",
            Direction.RIGHT, "goblin/right.gif");

    private final GameSession session;
    private final Assets assets;
    private final LevelArt art;

    private final Pane root = new Pane();
    private final ImageView background = new ImageView();
    private final Group debugLayer = new Group();
    private final ImageView playerSprite = new ImageView();
    private final List<ImageView> guardSprites = new ArrayList<>();
    private final List<Arc> visionCones = new ArrayList<>();
    private final Map<Motion, Map<Facing, Image>> playerImages = new EnumMap<>(Motion.class);
    private final List<ImageView> hearts = new ArrayList<>();
    private final Label timer = new Label();
    private final Label prompt = new Label();

    WorldView(GameSession session, Assets assets, ObservableBooleanValue showDebugOverlay) {
        this.session = session;
        this.assets = assets;
        this.art = LevelArt.forLevel(session.level().number());

        Rect bounds = session.level().bounds();
        root.setPrefSize(bounds.width(), bounds.height());
        root.setMinSize(bounds.width(), bounds.height());
        root.setMaxSize(bounds.width(), bounds.height());
        root.setClip(new Rectangle(bounds.width(), bounds.height()));

        background.setFitWidth(bounds.width());
        background.setFitHeight(bounds.height());

        debugLayer.visibleProperty().bind(showDebugOverlay);
        loadPlayerImages();

        root.getChildren().addAll(background, debugLayer);
        for (Guard guard : session.guards()) {
            Arc cone = new Arc();
            cone.setType(ArcType.ROUND);
            cone.setFill(Color.WHITE);
            cone.setOpacity(0.2);
            ImageView sprite = new ImageView();
            sprite.setFitWidth(Guard.WIDTH);
            sprite.setFitHeight(Guard.HEIGHT);
            visionCones.add(cone);
            guardSprites.add(sprite);
            root.getChildren().addAll(cone, sprite);
        }
        playerSprite.setFitWidth(Player.WIDTH);
        playerSprite.setFitHeight(Player.HEIGHT);
        root.getChildren().add(playerSprite);
        buildHud(session.level());

        refreshStaticLayers();
        render();
    }

    Pane node() {
        return root;
    }

    /** Background and debug shapes only change when a door is toggled. */
    void refreshStaticLayers() {
        boolean anyDoorOpen = session.level().doors().stream().anyMatch(door -> session.isDoorOpen(door.id()));
        background.setImage(assets.image(anyDoorOpen ? art.doorsOpen() : art.doorsClosed()));

        debugLayer.getChildren().clear();
        session.solids().forEach(wall -> debugLayer.getChildren().add(outline(wall, Color.RED)));
        for (DoorSpec door : session.level().doors()) {
            debugLayer.getChildren().add(outline(door.leverZone(), Color.LIME));
        }
        debugLayer.getChildren().add(outline(session.level().exitZone(), Color.GOLD));
    }

    void render() {
        Player player = session.player();
        playerSprite.setImage(playerImages.get(player.motion()).get(player.facing()));
        playerSprite.setX(player.bounds().x());
        playerSprite.setY(player.bounds().y());
        playerSprite.setOpacity(session.isInvulnerable() && blinkPhase() ? 0.35 : 1);

        List<Guard> guards = session.guards();
        for (int i = 0; i < guards.size(); i++) {
            Guard guard = guards.get(i);
            ImageView sprite = guardSprites.get(i);
            sprite.setImage(assets.image(GOBLIN_SPRITES.get(guard.facing())));
            sprite.setX(guard.bounds().x());
            sprite.setY(guard.bounds().y());
            drawCone(visionCones.get(i), guard.vision());
        }

        for (int i = 0; i < hearts.size(); i++) {
            hearts.get(i).setVisible(i < session.lives());
        }
        timer.setText(TimeFormat.minutesSeconds(TimeFormat.ofSeconds(session.elapsedSeconds())));
        String hint = session.availableInteraction().map(WorldView::hint).orElse("");
        prompt.setText(hint);
        prompt.setVisible(!hint.isEmpty());
    }

    private static String hint(Interaction interaction) {
        return switch (interaction) {
            case PULL_LEVER -> "E  -  pull the lever";
            case EXIT_LEVEL -> "E  -  escape";
        };
    }

    private void buildHud(LevelDefinition level) {
        HBox heartRow = new HBox(4);
        for (int i = 0; i < GameSession.STARTING_LIVES; i++) {
            ImageView heart = new ImageView(assets.image("ui/heart.png"));
            heart.setFitWidth(20);
            heart.setFitHeight(20);
            hearts.add(heart);
            heartRow.getChildren().add(heart);
        }
        heartRow.relocate(10, 8);

        Label name = new Label(level.number() + ". " + level.name());
        name.getStyleClass().add("hud-level");
        name.relocate(90, 9);

        timer.getStyleClass().add("hud-timer");
        timer.relocate(level.bounds().width() - 80, 6);

        prompt.getStyleClass().add("hud-prompt");
        prompt.layoutXProperty().bind(root.widthProperty().subtract(prompt.widthProperty()).divide(2));
        prompt.setLayoutY(level.bounds().height() - 34);

        root.getChildren().addAll(heartRow, name, timer, prompt);
    }

    private void loadPlayerImages() {
        playerImages.put(Motion.IDLE, sprites("player/idle-left.gif", "player/idle-right.gif"));
        playerImages.put(Motion.WALKING, sprites("player/walk-left.gif", "player/walk-right.gif"));
        playerImages.put(Motion.RUNNING, sprites("player/run-left.gif", "player/run-right.gif"));
    }

    private Map<Facing, Image> sprites(String left, String right) {
        Map<Facing, Image> byFacing = new EnumMap<>(Facing.class);
        byFacing.put(Facing.LEFT, assets.image(left));
        byFacing.put(Facing.RIGHT, assets.image(right));
        return byFacing;
    }

    private static void drawCone(Arc arc, VisionCone cone) {
        arc.setCenterX(cone.apex().x());
        arc.setCenterY(cone.apex().y());
        arc.setRadiusX(cone.range());
        arc.setRadiusY(cone.range());
        arc.setStartAngle(cone.headingDegrees() - cone.halfAngleDegrees());
        arc.setLength(cone.halfAngleDegrees() * 2);
    }

    private boolean blinkPhase() {
        return ((long) (session.elapsedSeconds() * 8)) % 2 == 0;
    }

    private static Rectangle outline(Rect rect, Color color) {
        Rectangle shape = new Rectangle(rect.x(), rect.y(), Math.max(rect.width(), 1), Math.max(rect.height(), 1));
        shape.setFill(color.deriveColor(0, 1, 1, 0.25));
        shape.setStroke(color);
        return shape;
    }
}
