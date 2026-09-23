package com.greedsgambit.score;

import com.greedsgambit.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreServiceTest {

    private static final Account ALICE = new Account("alice");
    private static final Account BOB = new Account("bob");

    @TempDir
    Path dataDir;

    private Path scoresFile;
    private ScoreService scores;

    @BeforeEach
    void setUp() {
        scoresFile = dataDir.resolve("scores.properties");
        scores = new ScoreService(new FileScoreStore(scoresFile));
    }

    @Test
    void keepsOnlyTheBestTime() {
        assertTrue(scores.recordCompletion(ALICE, 1, Duration.ofSeconds(90)));
        assertFalse(scores.recordCompletion(ALICE, 1, Duration.ofSeconds(95)));
        assertTrue(scores.recordCompletion(ALICE, 1, Duration.ofSeconds(80)));

        assertEquals(Optional.of(Duration.ofSeconds(80)), scores.bestTime(ALICE, 1));
    }

    @Test
    void finishingALevelUnlocksTheNextOne() {
        assertTrue(scores.isUnlocked(ALICE, 1));
        assertFalse(scores.isUnlocked(ALICE, 2));

        scores.recordCompletion(ALICE, 1, Duration.ofSeconds(60));

        assertTrue(scores.isUnlocked(ALICE, 2));
        assertFalse(scores.isUnlocked(BOB, 2), "progress is per player");
    }

    @Test
    void leaderboardIsFastestFirst() {
        scores.recordCompletion(ALICE, 1, Duration.ofSeconds(90));
        scores.recordCompletion(BOB, 1, Duration.ofSeconds(45));
        scores.recordCompletion(BOB, 2, Duration.ofSeconds(10));

        List<BestTime> board = scores.leaderboard(1, 10);

        assertEquals(List.of("bob", "alice"), board.stream().map(BestTime::username).toList());
        assertEquals(1, scores.leaderboard(1, 1).size());
    }

    @Test
    void scoresSurviveARestartAndCorruptLinesAreSkipped() throws IOException {
        scores.recordCompletion(ALICE, 1, Duration.ofMillis(61_500));
        Files.writeString(scoresFile, "garbage\n1.bob=not-a-number\n", java.nio.file.StandardOpenOption.APPEND);

        ScoreService afterRestart = new ScoreService(new FileScoreStore(scoresFile));

        assertEquals(Optional.of(Duration.ofMillis(61_500)), afterRestart.bestTime(ALICE, 1));
        assertEquals(1, afterRestart.leaderboard(1, 10).size());
    }
}
