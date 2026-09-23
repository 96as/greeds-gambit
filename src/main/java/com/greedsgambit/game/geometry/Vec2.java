package com.greedsgambit.game.geometry;

/** Immutable 2D vector in world units (pixels at 1x zoom, y grows downwards). */
public record Vec2(double x, double y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 plus(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 minus(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    public Vec2 times(double factor) {
        return new Vec2(x * factor, y * factor);
    }

    public double dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public double distanceTo(Vec2 other) {
        return minus(other).length();
    }

    /** Unit vector in the same direction, or {@link #ZERO} for the zero vector. */
    public Vec2 normalized() {
        double length = length();
        return length == 0 ? ZERO : new Vec2(x / length, y / length);
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }
}
