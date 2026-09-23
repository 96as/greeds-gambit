package com.greedsgambit.game;

import com.greedsgambit.game.geometry.Vec2;
import com.greedsgambit.game.level.GuardSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuardTest {

    private static GuardSpec patrol(Vec2... route) {
        return new GuardSpec("g", List.of(route), 10, 50, 30);
    }

    @Test
    void walksTowardsTheNextWaypointAndFacesIt() {
        Guard guard = new Guard(patrol(new Vec2(0, 0), new Vec2(100, 0)));

        guard.update(2);

        assertEquals(new Vec2(20, 0), guard.position());
        assertEquals(Direction.RIGHT, guard.facing());
    }

    @Test
    void turnsAroundAtTheEndOfItsRoute() {
        Guard guard = new Guard(patrol(new Vec2(0, 0), new Vec2(100, 0)));

        guard.update(12); // 120 units: 100 out, 20 back

        assertEquals(80, guard.position().x(), 1e-9);
        assertEquals(Direction.LEFT, guard.facing());
    }

    @Test
    void walksMultiPointRoutesBackAndForth() {
        Guard guard = new Guard(patrol(new Vec2(0, 0), new Vec2(0, 50), new Vec2(50, 50)));

        guard.update(7); // 70 units: down 50, right 20
        assertEquals(new Vec2(20, 50), guard.position());
        assertEquals(Direction.RIGHT, guard.facing());

        guard.update(5); // 50 more: right 30 to the end, back 20
        assertEquals(new Vec2(30, 50), guard.position());

        guard.update(5); // back 30 to the corner, then up 20
        assertEquals(new Vec2(0, 30), guard.position());
        assertEquals(Direction.UP, guard.facing());
    }

    @Test
    void visionFollowsTheGuard() {
        Guard guard = new Guard(patrol(new Vec2(0, 0), new Vec2(0, 100)));

        guard.update(1);

        assertEquals(guard.position(), guard.vision().apex());
        assertEquals(new Vec2(0, 1), guard.vision().direction());
    }

    @Test
    void routesNeedTwoDistinctWaypoints() {
        assertThrows(IllegalArgumentException.class, () -> patrol(new Vec2(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> patrol(new Vec2(1, 1), new Vec2(1, 1)));
    }
}
