package com.github.benisraelnir.gripme.core.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Reads Markdown content from a directory/file path.
 */
@RequiredArgsConstructor
public class DirectoryReader implements Reader {
    private final Path path;
    private FileTime lastModifiedTime;

    @Override
    public String read() throws Exception {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        byte[] content = Files.readAllBytes(path);
        lastModifiedTime = Files.getLastModifiedTime(path);
        return new String(content);
    }

    @Override
    public boolean hasChanged() throws Exception {
        if (!Files.exists(path) || lastModifiedTime == null) {
            return true;
        }

        FileTime currentModifiedTime = Files.getLastModifiedTime(path);
        return !currentModifiedTime.equals(lastModifiedTime);
    }
}
