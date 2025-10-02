package com.github.groundbreakingmc.tomly.writer;

import com.github.groundbreakingmc.tomly.Tomly;
import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Toml Writer Tests")
class TomlWriterTest {

    @Test
    @DisplayName("write() should save document to file")
    void write(@TempDir Path tempDir) throws URISyntaxException {
        final Path config = Path.of(Objects.requireNonNull(
                getClass().getResource("/test-data.toml")
        ).toURI());

        final TomlDocument document = Tomly.parse(config, true, PreserveOptions.defaultOptions());

        final Path outFile = tempDir.resolve("test.toml");
        document.save(outFile, WriterOptions.defaultOptions());

        final TomlDocument loaded = Tomly.parse(outFile, true, PreserveOptions.defaultOptions());
        final Map<String, Object> loadedRaw = loaded.raw();
        for (final Map.Entry<String, Object> entry : document.raw().entrySet()) {
            final Object value = loadedRaw.get(entry.getKey());
            assertNotNull(value);
            assertEquals(value, entry.getValue());
        }
    }
}