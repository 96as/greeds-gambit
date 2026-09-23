# Greed's Gambit

A top-down stealth game built with JavaFX. Sneak through the library, pull the lever to open the
study door, stay out of the goblins' sight and escape down the stairs.

## Requirements

- JDK 21 or newer (Maven is downloaded automatically by the wrapper)

## Run

```bash
./mvnw javafx:run
```

On Windows use `mvnw.cmd javafx:run`.

## Test

```bash
./mvnw verify
```

## Build a standalone app

```bash
./mvnw javafx:jlink
```

This creates a self-contained runtime in `target/greeds-gambit/`; start it with
`target/greeds-gambit/bin/greeds-gambit`.

## Controls

| Key | Action |
|-----|--------|
| W A S D / arrow keys | Move |
| Shift | Run |
| E | Pull a lever / take the exit |
| Esc | Pause |

## Save data

Accounts and best times are stored in `~/.greeds-gambit/`. Passwords are stored only as salted
PBKDF2 hashes. To use another folder (e.g. for testing), pass
`-Dgreedsgambit.home=/some/folder` to the JVM.

## Project layout

```
src/main/java/com/greedsgambit/
  GreedsGambitApp.java   entry point; wires the services together
  game/                  game rules - pure Java, no JavaFX
    geometry/            Vec2, Rect, VisionCone
    level/               level data (walls, doors, guards, exits)
  account/               sign-up, login, password hashing
  score/                 best times, scoreboard, level unlocking
  storage/               crash-safe save files
  ui/                    JavaFX: navigation, menus, game rendering
src/main/resources/com/greedsgambit/
  css/ fonts/ images/
src/test/java/           unit tests (rules, level data, accounts, scores, architecture)
docs/ARCHITECTURE.md     design, trade-offs, and an audit of the original version
```

## Adding a level

1. Add a `LevelDefinition` in `game/level/Levels.java` and include it in `ALL`.
2. Add its background image(s) under `images/levels/` and register them in `ui/game/LevelArt.java`.
3. Run `./mvnw verify`. `LevelsTest` checks that the spawn is valid and that the exit can
   actually be reached.

Turn on **Settings → Show walls and trigger zones** to see walls, levers and exits while tuning
coordinates.

## Credits

See [CREDITS.md](CREDITS.md).
