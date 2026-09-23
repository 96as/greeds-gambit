package com.greedsgambit.game.geometry;

import java.util.Collection;

/**
 * A guard's field of view: a circular sector starting at {@code apex}, pointing along
 * {@code direction}, reaching {@code range} units and spanning {@code halfAngleDegrees} to each side.
 */
public record VisionCone(Vec2 apex, Vec2 direction, double range, double halfAngleDegrees) {

    public VisionCone {
        direction = direction.normalized();
        if (direction.isZero()) {
            throw new IllegalArgumentException("A vision cone needs a direction");
        }
        if (range <= 0 || halfAngleDegrees <= 0 || halfAngleDegrees > 180) {
            throw new IllegalArgumentException("Invalid cone: range=" + range + ", halfAngle=" + halfAngleDegrees);
        }
    }

    public boolean contains(Vec2 point) {
        Vec2 offset = point.minus(apex);
        double distance = offset.length();
        if (distance > range) {
            return false;
        }
        if (distance == 0) {
            return true;
        }
        return offset.dot(direction) >= distance * Math.cos(Math.toRadians(halfAngleDegrees));
    }

    /**
     * True when any part of {@code body} is inside the cone with an unobstructed line of sight.
     *
     * @param occluders solid areas that block vision (walls, closed doors)
     */
    public boolean sees(Rect body, Collection<Rect> occluders) {
        for (Vec2 point : body.samplePoints()) {
            if (contains(point) && hasLineOfSight(point, occluders)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLineOfSight(Vec2 target, Collection<Rect> occluders) {
        for (Rect occluder : occluders) {
            if (occluder.intersectsSegment(apex, target)) {
                return false;
            }
        }
        return true;
    }

    /** Heading in degrees, counter-clockwise from east as seen on screen (the JavaFX Arc convention). */
    public double headingDegrees() {
        return Math.toDegrees(Math.atan2(-direction.y(), direction.x()));
    }
}
