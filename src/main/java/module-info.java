/**
 * Greed's Gambit.
 *
 * <p>Package layout (dependencies point downwards only):
 * <pre>
 *   com.greedsgambit            composition root (wires everything together)
 *   com.greedsgambit.ui.*       JavaFX screens, rendering, keyboard input
 *   com.greedsgambit.account    sign-up / login, password hashing
 *   com.greedsgambit.score      best times, level unlocking
 *   com.greedsgambit.game.*     pure game rules - no JavaFX, fully unit-tested
 * </pre>
 */
module com.greedsgambit {
    requires javafx.controls;

    exports com.greedsgambit to javafx.graphics;
}
