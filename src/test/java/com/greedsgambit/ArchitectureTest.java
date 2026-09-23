package com.greedsgambit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Keeps the layering described in docs/ARCHITECTURE.md from eroding over time. */
class ArchitectureTest {

    private static final Path SOURCES = Path.of("src/main/java/com/greedsgambit");
    private static final List<String> UI_FREE_PACKAGES = List.of("game", "account", "score", "storage", "util");

    @Test
    void gameRulesAndServicesDoNotDependOnJavaFx() {
        List<String> offenders = UI_FREE_PACKAGES.stream()
                .flatMap(pkg -> javaFiles(SOURCES.resolve(pkg)))
                .filter(file -> read(file).contains("import javafx."))
                .map(Path::toString)
                .toList();

        assertEquals(List.of(), offenders);
    }

    @Test
    void gameRulesDoNotDependOnPersistenceOrUi() {
        List<String> offenders = javaFiles(SOURCES.resolve("game"))
                .filter(file -> {
                    String source = read(file);
                    return source.contains("import com.greedsgambit.ui")
                            || source.contains("import com.greedsgambit.account")
                            || source.contains("import com.greedsgambit.score")
                            || source.contains("import com.greedsgambit.storage");
                })
                .map(Path::toString)
                .toList();

        assertEquals(List.of(), offenders);
    }

    private static Stream<Path> javaFiles(Path directory) {
        try {
            return Files.walk(directory).filter(path -> path.toString().endsWith(".java")).toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
