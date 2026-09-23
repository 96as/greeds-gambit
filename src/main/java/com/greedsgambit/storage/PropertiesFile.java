package com.greedsgambit.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * A small key/value file. Writes go to a temporary file that is then moved over the original,
 * so a crash mid-save never leaves a half-written file behind.
 */
public final class PropertiesFile {

    private final Path path;

    public PropertiesFile(Path path) {
        this.path = path;
    }

    public Properties load() {
        Properties properties = new Properties();
        if (Files.notExists(path)) {
            return properties;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new StorageException("Could not read " + path, e);
        }
    }

    public void save(Properties properties) {
        try {
            Path directory = path.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temp = Files.createTempFile(directory, path.getFileName().toString(), ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                properties.store(writer, null);
            }
            moveIntoPlace(temp);
        } catch (IOException e) {
            throw new StorageException("Could not write " + path, e);
        }
    }

    private void moveIntoPlace(Path temp) throws IOException {
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
