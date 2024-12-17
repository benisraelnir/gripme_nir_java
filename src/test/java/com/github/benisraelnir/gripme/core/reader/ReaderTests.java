package com.github.benisraelnir.gripme.core.reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReaderTests {
    @Test
    void textReaderShouldReturnContent() {
        String content = "# Test Content";
        Reader reader = new TextReader(content);

        assertDoesNotThrow(() -> {
            assertEquals(content, reader.read(null));
            assertFalse(reader.hasChanged());
        });
    }

    @Test
    void directoryReaderShouldReadFile(@TempDir Path tempDir) throws Exception {
        String content = "# Test File";
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, content);

        Reader reader = new DirectoryReader(tempDir);

        assertEquals(content, reader.read("test.md"));
        assertFalse(reader.hasChanged());

        // Ensure enough time passes for filesystem to register the change
        TimeUnit.MILLISECONDS.sleep(100);

        // Test file modification detection
        Files.writeString(file, content + " Updated");
        assertTrue(reader.hasChanged());
    }

    @Test
    void directoryReaderShouldThrowForNonExistentFile() {
        Reader reader = new DirectoryReader(Path.of("nonexistent.md"));

        assertThrows(IllegalArgumentException.class, () -> reader.read(null));
    }

    @Test
    void stdinReaderShouldReadFromInputStream() throws Exception {
        String testInput = "# Test Input\nSecond line";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(testInput.getBytes());
        Reader reader = new StdinReader(inputStream);

        String result = reader.read(null);
        assertEquals(testInput, result);
        assertFalse(reader.hasChanged());

        // Second read should return cached content
        assertEquals(testInput, reader.read(null));
    }
}
