package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Rect;
import com.greedsgambit.game.geometry.Vec2;
import com.greedsgambit.game.geometry.VisionCone;
import com.greedsgambit.game.level.GuardSpec;

import java.util.List;

/** A goblin patrolling its route back and forth, looking the way it walks. */
public final class Guard {

    public static final double WIDTH = 53;
    public static final double HEIGHT = 50;

    private final GuardSpec spec;
    private Vec2 position;
    private Vec2 heading;
    private int targetIndex = 1;
    private int step = 1;

    Guard(GuardSpec spec) {
        this.spec = spec;
        List<Vec2> route = spec.route();
        this.position = route.get(0);
        this.heading = route.get(1).minus(route.get(0)).normalized();
    }

    public String id() {
        return spec.id();
    }

    /** Centre of the guard. */
    public Vec2 position() {
        return position;
    }

    public Direction facing() {
        return Direction.of(heading);
    }

    public Rect bounds() {
        return Rect.centeredAt(position, WIDTH, HEIGHT);
    }

    public VisionCone vision() {
        return new VisionCone(position, heading, spec.visionRange(), spec.visionHalfAngle());
    }

    void update(double dt) {
        double remaining = spec.speed() * dt;
        List<Vec2> route = spec.route();
        while (remaining > 0) {
            Vec2 target = route.get(targetIndex);
            Vec2 toTarget = target.minus(position);
            double distance = toTarget.length();
            if (distance > remaining) {
                heading = toTarget.normalized();
                position = position.plus(heading.times(remaining));
                return;
            }
            position = target;
            remaining -= distance;
            advanceTarget(route.size());
            heading = route.get(targetIndex).minus(position).normalized();
        }
    }

    /** Ping-pong through the waypoints: 0, 1, ..., n-1, n-2, ..., 0, 1, ... */
    private void advanceTarget(int waypointCount) {
        if (targetIndex + step < 0 || targetIndex + step >= waypointCount) {
            step = -step;
        }
        targetIndex += step;
    }
}
