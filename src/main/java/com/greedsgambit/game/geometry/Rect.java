package com.greedsgambit.game.geometry;

import java.util.List;

/** Immutable axis-aligned rectangle. {@code (x, y)} is the top-left corner. */
public record Rect(double x, double y, double width, double height) {

    public Rect {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Negative size: " + width + "x" + height);
        }
    }

    /**
     * Builds the solid area of a straight wall drawn as a line with the given stroke thickness.
     * This matches how the level walls were originally traced over the background art.
     */
    public static Rect ofLine(double x1, double y1, double x2, double y2, double thickness) {
        double half = thickness / 2;
        double minX = Math.min(x1, x2) - half;
        double minY = Math.min(y1, y2) - half;
        return new Rect(minX, minY, Math.abs(x2 - x1) + thickness, Math.abs(y2 - y1) + thickness);
    }

    public static Rect centeredAt(Vec2 center, double width, double height) {
        return new Rect(center.x() - width / 2, center.y() - height / 2, width, height);
    }

    public double maxX() {
        return x + width;
    }

    public double maxY() {
        return y + height;
    }

    public Vec2 topLeft() {
        return new Vec2(x, y);
    }

    public Vec2 center() {
        return new Vec2(x + width / 2, y + height / 2);
    }

    public Rect movedTo(double newX, double newY) {
        return new Rect(newX, newY, width, height);
    }

    public Rect translated(double dx, double dy) {
        return new Rect(x + dx, y + dy, width, height);
    }

    /** True when the rectangles overlap by a positive area; touching edges do not count. */
    public boolean intersects(Rect other) {
        return x < other.maxX() && other.x < maxX() && y < other.maxY() && other.y < maxY();
    }

    public boolean contains(Vec2 point) {
        return point.x() >= x && point.x() <= maxX() && point.y() >= y && point.y() <= maxY();
    }

    /** Corners, edge midpoints and centre - used to test partial visibility of a body. */
    public List<Vec2> samplePoints() {
        double midX = x + width / 2;
        double midY = y + height / 2;
        return List.of(
                new Vec2(x, y), new Vec2(midX, y), new Vec2(maxX(), y),
                new Vec2(x, midY), new Vec2(midX, midY), new Vec2(maxX(), midY),
                new Vec2(x, maxY()), new Vec2(midX, maxY()), new Vec2(maxX(), maxY()));
    }

    /** Whether the segment {@code from -> to} passes through this rectangle (Liang-Barsky clipping). */
    public boolean intersectsSegment(Vec2 from, Vec2 to) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {from.x() - x, maxX() - from.x(), from.y() - y, maxY() - from.y()};
        double enter = 0;
        double exit = 1;
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) {
                    return false;
                }
            } else {
                double t = q[i] / p[i];
                if (p[i] < 0) {
                    enter = Math.max(enter, t);
                } else {
                    exit = Math.min(exit, t);
                }
                if (enter > exit) {
                    return false;
                }
            }
        }
        return true;
    }
}
