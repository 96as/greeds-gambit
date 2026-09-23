# Greed's Gambit — Architecture

This document explains how the game is put together, why, and what to revisit as it grows.
It replaces the original single-class implementation (`HelloApplication.java`, 1,790 lines).

---

## 1. Requirements

### Functional

| # | Requirement | Where |
|---|-------------|-------|
| F1 | Players sign up and log in with a username and password | `account` |
| F2 | Main menu: play, scoreboard, settings, help, log out, exit | `ui.menu` |
| F3 | Top-down stealth levels: walk / run, walls block movement | `game` |
| F4 | Goblin guards patrol routes and catch the player on sight or contact | `game.Guard`, `VisionCone` |
| F5 | Three lives; caught ⇒ lose a life and respawn; no lives ⇒ game over | `GameSession` |
| F6 | Levers open doors; standing on the exit and pressing **E** finishes the level | `GameSession` |
| F7 | Level timer; best time per player per level; scoreboard | `score` |
| F8 | Finishing a level unlocks the next | `ScoreService.isUnlocked` |
| F9 | Pause, restart, return to menu | `GameScreen` |

### Non-functional

| Concern | Target | How |
|---------|--------|-----|
| Correctness | Game rules testable without a screen | Pure-Java `game` package, 60+ unit tests |
| Security | No plaintext passwords | PBKDF2-HMAC-SHA512, 210k iterations, per-user salt |
| Responsiveness | UI never freezes | Password hashing on a background thread |
| Determinism | Same speed on 60 Hz and 144 Hz screens | Fixed 120 Hz simulation step |
| Durability | A crash never corrupts save files | Write-to-temp then atomic rename |
| Portability | Runs from a packaged image, any working directory | All assets loaded from the classpath |
| Maintainability | Adding a level = adding data | `LevelDefinition` records + validation tests |

### Constraints

- Java 21 (LTS) + JavaFX 21 (LTS). Single-player desktop app, no server.
- Small student team: prefer plain Java and the JDK over frameworks.

---

## 2. High-level design

```
                        ┌──────────────────────────────┐
                        │   GreedsGambitApp            │  composition root:
                        │   (creates & wires services) │  the only place that
                        └──────────────┬───────────────┘  knows concrete classes
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        ▼                              ▼                              ▼
┌───────────────┐            ┌───────────────────┐           ┌────────────────┐
│ ui            │            │ account           │           │ score          │
│  AppRouter    │──uses────▶ │  AccountService   │           │  ScoreService  │
│  menu.*       │──uses──────┼───────────────────┼─────────▶ │  ScoreStore    │
│  game.*       │            │  PasswordHasher   │           └───────┬────────┘
│  (JavaFX)     │            │  CredentialStore  │                   │
└──────┬────────┘            └─────────┬─────────┘                   │
       │ reads state /                 │                             │
       │ sends input                   ▼                             ▼
       ▼                       ┌───────────────────────────────────────────┐
┌───────────────┐              │ storage.PropertiesFile (atomic writes)    │
│ game          │              │   ~/.greeds-gambit/accounts.properties    │
│  GameSession  │              │   ~/.greeds-gambit/scores.properties      │
│  Player/Guard │              └───────────────────────────────────────────┘
│  level.*      │
│  geometry.*   │   ◀── no JavaFX, no I/O: enforced by ArchitectureTest
└───────────────┘
```

**Dependency rule:** arrows point one way. `game` depends on nothing but the JDK;
`account` and `score` depend on `storage`; only `ui` knows about JavaFX.
`ArchitectureTest` fails the build if `game`, `account`, `score`, `storage` or `util`
import `javafx.*`, or if `game` imports any other layer.

### Packages

| Package | Responsibility |
|---------|----------------|
| `com.greedsgambit` | `GreedsGambitApp` — entry point and wiring |
| `game.geometry` | `Vec2`, `Rect`, `VisionCone` — immutable maths |
| `game.level` | `LevelDefinition`, `GuardSpec`, `DoorSpec`, `Levels` (the campaign data) |
| `game` | `GameSession` (rules), `Player`, `Guard`, `InputSnapshot`, `GameEvent` |
| `account` | Sign-up / login rules, password hashing, credential persistence |
| `score` | Best times, leaderboard, level unlocking |
| `storage` | Crash-safe properties files |
| `ui` | `AppRouter` (navigation), `Screen`, `Theme`, `Assets`, `Settings` |
| `ui.menu` | One class per menu page |
| `ui.game` | `GameScreen`, `WorldView` (rendering), `KeyboardInput`, `FixedStepLoop` |

### Screen flow

```
Welcome ──▶ Log in ──┐
   │                 ├──▶ Main menu ──▶ Levels ──▶ Game ──▶ (pause | game over | escaped)
   └──▶ Sign up ─────┘        │                                   │
                              ├──▶ Scoreboard                     └──▶ next level / retry / menu
                              ├──▶ Settings
                              └──▶ Help
```

There is **one `Scene`**; `AppRouter.show()` swaps its root and calls `onHide()` on the old
screen (so the game loop and listeners are released) and `onShow()` on the new one.

---

## 3. Deep dive

### 3.1 Game loop and data flow

```
 keyboard ─▶ KeyboardInput ──poll()──▶ InputSnapshot
                                         │
 AnimationTimer (every frame)            ▼
   FixedStepLoop ── n × 1/120 s ──▶ GameSession.update(dt, input) ──▶ List<GameEvent>
        │                                 │                                   │
        │                                 │ mutates Player, Guards, doors     ▼
        └── once per frame ─▶ WorldView.render()  ◀── reads state      GameScreen reacts:
                                                                        door art, overlays,
                                                                        save best time
```

- **Held keys, not key-repeat.** The original moved 5 px per `KEY_PRESSED`, so speed depended
  on the OS key-repeat rate. `KeyboardInput` tracks held keys; the model moves at
  `speed × dt`. Interact (**E**) is edge-triggered so one press = one action.
- **Fixed time step.** Simulation always advances in 1/120 s steps; frames that take longer run
  several steps (capped at 0.25 s to avoid a "spiral of death" after a stall).
- **Events out, state read back.** `GameSession` never calls the UI. It returns sealed
  `GameEvent`s (`PlayerCaught`, `DoorToggled`, `LevelCompleted`, `GameOver`) and the UI decides
  how to present them. The pattern-matching `switch` in `GameScreen.step` is exhaustive, so
  adding an event type is a compile error until the UI handles it.

### 3.2 Collision

Walls were traced as lines over the art; `Rect.ofLine(x1, y1, x2, y2, thickness)` turns each
into the solid rectangle the stroke covers. `Player.move` resolves **X then Y separately**
and snaps to the wall edge, so pushing diagonally into a wall slides along it instead of
stopping dead. Solids the player already overlaps are ignored, so a door closing on the player
can never trap them.

### 3.3 Vision

`VisionCone` is a circular sector (apex, heading, range, half-angle). A guard sees the player if
any of 9 sample points on the player's box is inside the sector **and** the segment from the
guard to that point crosses no solid (Liang–Barsky clipping). The original used the
*bounding box* of the drawn arc — much larger than the visible cone — and ignored walls.

### 3.4 Level data

A level is a `LevelDefinition` record: bounds, spawn, walls, doors (wall + lever zone),
guards (route, speed, vision), exit zone. Artwork is mapped separately in `ui.game.LevelArt`
so the rules never reference file names.

`LevelsTest` validates every level on each build:

- numbered 1..n without gaps;
- spawn is inside the level and not in a wall; exit and guard routes are inside the level;
- every lever is reachable, and the exit is reachable once doors are open —
  checked with a flood fill of every position the player's body can occupy;
- level 1's exit is **not** reachable before the lever is pulled (the puzzle actually works).

### 3.5 Accounts

| Aspect | Original | Now |
|--------|----------|-----|
| Storage | `game logins.txt` in the working directory | `~/.greeds-gambit/accounts.properties` (override: `-Dgreedsgambit.home=…`) |
| Password | Plaintext | `pbkdf2-sha512$<iterations>$<salt>$<hash>` |
| Match | `line.contains("Username: " + name)` — `sa`/`12` logged in as `Salameh`/`1234` | Exact, case-insensitive lookup; constant-time hash comparison |
| Validation | None | Username `[A-Za-z0-9_]{3,16}`, password 4–64 chars, confirmation |
| Threading | On the UI thread | Background executor, UI disabled while working |

The hash string records its own iteration count, so the work factor can be raised later and old
accounts still verify (re-hash on next login would be the upgrade path).

### 3.6 Persistence

Both stores are small `.properties` files behind interfaces (`CredentialStore`, `ScoreStore`).
`PropertiesFile.save` writes a temp file in the same directory and atomically renames it over
the original. Corrupt score lines are skipped rather than failing the whole scoreboard.

---

## 4. Scale and reliability

This is a single-player desktop game, so "scale" means *content* and *team size*, not traffic.

| Growth | Impact | Plan |
|--------|--------|------|
| 10+ levels | `Levels.java` gets long | Move level data to JSON/YAML files in resources; keep `LevelsTest` validating them |
| Many guards / entities | Per-step cost O(guards × walls) — trivial today (2 × 20) | Spatial grid for walls if it ever matters |
| Hundreds of accounts | Properties file is read per call | Fine to ~10⁴ users; beyond that, SQLite |
| Online leaderboard | `ScoreStore` is an interface | Add an HTTP-backed implementation; keep the file one as offline cache |

**Failure handling.** Storage errors surface as `StorageException`; the UI shows a message and
keeps running (a failed best-time save does not lose the level result). The game pauses
automatically when the window loses focus, and held keys are cleared so none get "stuck".

---

## 5. Trade-offs

| Decision | Chosen | Alternative | Why |
|----------|--------|-------------|-----|
| Rendering | Scene-graph nodes (`ImageView`, `Arc`) | `Canvas` redraw | Animated GIF sprites work for free; node count is tiny |
| Level format | Java records in code | JSON files | Compile-time checked, no parser dependency; revisit at ~10 levels |
| Boundaries | Packages + `ArchitectureTest` | Maven multi-module | Same guarantee, far less build ceremony for a small team |
| Persistence | `.properties` files | SQLite / H2 | Zero dependencies, human-readable; interfaces keep the swap cheap |
| Password KDF | PBKDF2 (JDK built-in) | Argon2 / bcrypt | No third-party crypto dependency; OWASP-compliant parameters |
| Dependencies | `javafx-controls` only | FXGL, ControlsFX, FormsFX, BootstrapFX (original) | None were used; FXGL alone is tens of MB |
| Navigation | Swap root of one `Scene` | New `Scene` per page (original) | No window resize/flicker; one stylesheet |
| Screen size | World is 800×400, scaled to fit the window | Fixed window sizes per scene | Window can be resized; menus and game share one window |

---

## 6. What to revisit

1. **Level editor / data files** once there are more than a handful of levels.
2. **Audio** — the original's volume slider had no audio behind it, so it was removed; add a
   `SoundService` behind an interface when sounds exist.
3. **Level 2 has no guards** (it was unfinished in the original). Its exit zone at the bottom
   corridor is a placeholder chosen from the artwork.
4. **Re-hash on login** when `PasswordHasher.DEFAULT_ITERATIONS` is raised.
5. **UI tests** with TestFX if the menus grow beyond simple forms.
6. **Asset licensing** — see `CREDITS.md`; confirm the licenses before any public release.

---

## Appendix — audit of the original implementation

Found while porting `HelloApplication.java`. Each is fixed or removed in this version.

**Security / data**
- Plaintext passwords in `game logins.txt`, read from the current working directory.
- `contains()` matching let partial usernames/passwords log in, and made any username that is a
  substring of an existing one appear "taken".
- Sign-up gave no feedback on success.

**Gameplay bugs**
- Exit detection used `idle.getY() == 315` — exact floating-point equality, anywhere on that row.
- Level 2: running moved the *level 1* sprites; W checked collisions against a level 1 sprite;
  A/D/S had no collision at all.
- Level 2's last wall (`borders1[34]`) was never added — the loop ran to `i < 34`
  (the code comment says *"not appearing for some reason"*).
- Guard 1's sprites and vision were never added to the scene, so only one goblin existed.
- Vision used the bounding box of the drawn arc and saw through walls.
- At zero lives nothing happened; game-over and restart were commented out, and "restart"
  only switched scenes without resetting state.
- The Help page had no way back to the menu.
- Movement speed depended on the OS key-repeat rate; each character was six `ImageView`s moved
  in lockstep, printing 12 lines to the console per key press.

**Structure / build**
- One class; `start()` alone was ~1,400 lines with copy-pasted blocks per level.
- The timer used a `Timeline` that scheduled a new `java.util.Timer` task every second
  (a non-daemon thread) alongside a separate minutes `Timeline`.
- Dead code: `reset()`, `first_start`, a `for (i <= 1)` loop, `HelloController` and an FXML
  file pointing at a package that did not exist.
- Unused dependencies (FXGL, ControlsFX, FormsFX, BootstrapFX, FXML) and an early-access
  JavaFX build (`21-ea+24`).
- `menu.css` loaded via `new File("menu.css")`, which breaks when packaged or run elsewhere.
- Duplicate assets (three identical backgrounds) and many unused sprites.
