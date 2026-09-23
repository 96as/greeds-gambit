package com.greedsgambit.score;

import com.greedsgambit.storage.PropertiesFile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Stores best times in a properties file as {@code <level>.<username>=<milliseconds>}. */
public final class FileScoreStore implements ScoreStore {

    private final PropertiesFile file;

    public FileScoreStore(Path path) {
        this.file = new PropertiesFile(path);
    }

    @Override
    public synchronized List<BestTime> all() {
        Properties scores = file.load();
        List<BestTime> result = new ArrayList<>();
        for (String key : scores.stringPropertyNames()) {
            int dot = key.indexOf('.');
            if (dot <= 0) {
                continue;
            }
            try {
                int level = Integer.parseInt(key.substring(0, dot));
                long millis = Long.parseLong(scores.getProperty(key));
                result.add(new BestTime(key.substring(dot + 1), level, Duration.ofMillis(millis)));
            } catch (NumberFormatException e) {
                // A hand-edited or corrupted line: skip it rather than lose every other score.
            }
        }
        return result;
    }

    @Override
    public synchronized void save(BestTime bestTime) {
        Properties scores = file.load();
        scores.setProperty(bestTime.levelNumber() + "." + bestTime.username(),
                Long.toString(bestTime.time().toMillis()));
        file.save(scores);
    }
}
