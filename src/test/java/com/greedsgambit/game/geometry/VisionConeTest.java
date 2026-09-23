package com.greedsgambit.game.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionConeTest {

    /** Looking down the screen from (100, 100), 100 units far, 45 degrees to each side. */
    private final VisionCone lookingDown = new VisionCone(new Vec2(100, 100), new Vec2(0, 1), 100, 45);

    @Test
    void seesPointsInFrontWithinRangeAndAngle() {
        assertTrue(lookingDown.contains(new Vec2(100, 150)));
        assertTrue(lookingDown.contains(new Vec2(140, 150)), "within 45 degrees");
    }

    @Test
    void doesNotSeeBehindBesideOrTooFar() {
        assertFalse(lookingDown.contains(new Vec2(100, 50)), "behind");
        assertFalse(lookingDown.contains(new Vec2(160, 110)), "outside the angle");
        assertFalse(lookingDown.contains(new Vec2(100, 201)), "out of range");
    }

    @Test
    void wallsBlockLineOfSight() {
        Rect body = new Rect(90, 160, 20, 20);
        Rect wallBetween = new Rect(50, 130, 100, 5);

        assertTrue(lookingDown.sees(body, List.of()));
        assertFalse(lookingDown.sees(body, List.of(wallBetween)));
    }

    @Test
    void seesABodyThatIsOnlyPartlyInsideTheCone() {
        Rect mostlyOutside = new Rect(130, 150, 40, 40);

        assertTrue(lookingDown.sees(mostlyOutside, List.of()));
    }

    @Test
    void headingUsesScreenCounterClockwiseDegrees() {
        assertEquals(-90, lookingDown.headingDegrees(), 1e-9);
        assertEquals(0, new VisionCone(Vec2.ZERO, new Vec2(1, 0), 1, 10).headingDegrees(), 1e-9);
    }
}
