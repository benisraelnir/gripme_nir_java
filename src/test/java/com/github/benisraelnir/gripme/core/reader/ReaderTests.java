package com.github.benisraelnir.gripme.core.reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReaderTests {
    @Test
    void textReaderShouldReturnContent() {
        String content = "# Test Content";
        Reader reader = new TextReader(content);

        assertDoesNotThrow(() -> {
            assertEquals(content, reader.read());
            assertFalse(reader.hasChanged());
        });
    }

    @Test
    void directoryReaderShouldReadFile(@TempDir Path tempDir) throws Exception {
        String content = "# Test File";
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, content);

        Reader reader = new DirectoryReader(file);

        assertEquals(content, reader.read());
        assertFalse(reader.hasChanged());

        // Test file modification detection
        Files.writeString(file, content + " Updated");
        assertTrue(reader.hasChanged());
    }

    @Test
    void directoryReaderShouldThrowForNonExistentFile() {
        Reader reader = new DirectoryReader(Path.of("nonexistent.md"));

        assertThrows(IllegalArgumentException.class, reader::read);
    }

    @Test
    void stdinReaderShouldCacheContent() throws Exception {
        // Note: This test assumes no actual stdin input,
        // just verifies caching behavior
        Reader reader = new StdinReader();
        String firstRead = reader.read();
        String secondRead = reader.read();

        assertNotNull(firstRead);
        assertEquals(firstRead, secondRead);
        assertFalse(reader.hasChanged());
    }
}
