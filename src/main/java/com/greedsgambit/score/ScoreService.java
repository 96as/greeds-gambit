package com.greedsgambit.score;

import com.greedsgambit.account.Account;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Best times, the scoreboard, and which levels a player has unlocked. */
public final class ScoreService {

    private final ScoreStore store;

    public ScoreService(ScoreStore store) {
        this.store = store;
    }

    /**
     * Records a finished level. Returns true if it beat the player's previous best
     * (or was their first completion).
     */
    public boolean recordCompletion(Account account, int levelNumber, Duration time) {
        Optional<Duration> previous = bestTime(account, levelNumber);
        if (previous.isPresent() && previous.get().compareTo(time) <= 0) {
            return false;
        }
        store.save(new BestTime(account.username(), levelNumber, time));
        return true;
    }

    public Optional<Duration> bestTime(Account account, int levelNumber) {
        return store.all().stream()
                .filter(score -> score.levelNumber() == levelNumber)
                .filter(score -> score.username().equals(account.username()))
                .map(BestTime::time)
                .findFirst();
    }

    /** Level 1 is always open; each later level opens once the one before it is finished. */
    public boolean isUnlocked(Account account, int levelNumber) {
        return levelNumber <= 1 || bestTime(account, levelNumber - 1).isPresent();
    }

    /** Fastest players first. */
    public List<BestTime> leaderboard(int levelNumber, int limit) {
        return store.all().stream()
                .filter(score -> score.levelNumber() == levelNumber)
                .sorted(Comparator.comparing(BestTime::time).thenComparing(BestTime::username))
                .limit(limit)
                .toList();
    }
}
