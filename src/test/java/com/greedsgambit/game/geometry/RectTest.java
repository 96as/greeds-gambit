package com.greedsgambit.game.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectTest {

    @Test
    void wallLineBecomesRectangleIncludingStroke() {
        Rect wall = Rect.ofLine(300, 151, 300, 31, 3);

        assertEquals(new Rect(298.5, 29.5, 3, 123), wall);
    }

    @Test
    void touchingEdgesDoNotCountAsOverlap() {
        Rect a = new Rect(0, 0, 10, 10);

        assertFalse(a.intersects(new Rect(10, 0, 10, 10)));
        assertTrue(a.intersects(new Rect(9.9, 0, 10, 10)));
    }

    @Test
    void segmentThroughRectangleIntersects() {
        Rect box = new Rect(10, 10, 10, 10);

        assertTrue(box.intersectsSegment(new Vec2(0, 15), new Vec2(30, 15)));
        assertTrue(box.intersectsSegment(new Vec2(12, 12), new Vec2(13, 13)), "fully inside");
        assertFalse(box.intersectsSegment(new Vec2(0, 0), new Vec2(30, 0)), "passes above");
        assertFalse(box.intersectsSegment(new Vec2(0, 15), new Vec2(5, 15)), "stops short");
    }

    @Test
    void rejectsNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new Rect(0, 0, -1, 5));
    }
}
