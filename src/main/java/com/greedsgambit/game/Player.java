package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;

import java.util.Collection;

/** The thief. Holds position and animation state; {@link GameSession} decides where it may move. */
public final class Player {

    public static final double WIDTH = 32;
    public static final double HEIGHT = 36;
    public static final double WALK_SPEED = 100;
    public static final double RUN_SPEED = 200;

    private Rect bounds;
    private Facing facing = Facing.RIGHT;
    private Motion motion = Motion.IDLE;

    Player(Vec2 topLeft) {
        this.bounds = new Rect(topLeft.x(), topLeft.y(), WIDTH, HEIGHT);
    }

    public Rect bounds() {
        return bounds;
    }

    public Facing facing() {
        return facing;
    }

    public Motion motion() {
        return motion;
    }

    void teleport(Vec2 topLeft) {
        bounds = bounds.movedTo(topLeft.x(), topLeft.y());
        motion = Motion.IDLE;
    }

    /**
     * Moves according to {@code input}, sliding along anything solid instead of passing through it.
     * Each axis is resolved separately so the player can glide along a wall while pushing into it.
     * A solid the player already overlaps (e.g. a door closed on top of them) does not block, so
     * they can always walk out of it.
     */
    void move(InputSnapshot input, double dt, Collection<Rect> solids, Rect worldBounds) {
        double dx = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        double dy = (input.down() ? 1 : 0) - (input.up() ? 1 : 0);
        Vec2 direction = new Vec2(dx, dy).normalized();

        if (direction.isZero()) {
            motion = Motion.IDLE;
            return;
        }
        motion = input.run() ? Motion.RUNNING : Motion.WALKING;
        if (dx != 0) {
            facing = dx < 0 ? Facing.LEFT : Facing.RIGHT;
        }

        double distance = (input.run() ? RUN_SPEED : WALK_SPEED) * dt;
        bounds = slideX(bounds, direction.x() * distance, solids);
        bounds = slideY(bounds, direction.y() * distance, solids);
        bounds = clampInside(bounds, worldBounds);
    }

    private static Rect slideX(Rect body, double dx, Collection<Rect> solids) {
        if (dx == 0) {
            return body;
        }
        Rect moved = body.translated(dx, 0);
        for (Rect solid : solids) {
            if (moved.intersects(solid) && !body.intersects(solid)) {
                moved = moved.movedTo(dx > 0 ? solid.x() - body.width() : solid.maxX(), moved.y());
            }
        }
        return moved;
    }

    private static Rect slideY(Rect body, double dy, Collection<Rect> solids) {
        if (dy == 0) {
            return body;
        }
        Rect moved = body.translated(0, dy);
        for (Rect solid : solids) {
            if (moved.intersects(solid) && !body.intersects(solid)) {
                moved = moved.movedTo(moved.x(), dy > 0 ? solid.y() - body.height() : solid.maxY());
            }
        }
        return moved;
    }

    private static Rect clampInside(Rect body, Rect area) {
        double x = Math.clamp(body.x(), area.x(), area.maxX() - body.width());
        double y = Math.clamp(body.y(), area.y(), area.maxY() - body.height());
        return body.movedTo(x, y);
    }
}
