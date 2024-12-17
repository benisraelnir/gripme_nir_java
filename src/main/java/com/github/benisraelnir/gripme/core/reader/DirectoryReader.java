package com.github.benisraelnir.gripme.core.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Reads Markdown content from a directory/file path.
 */
public class DirectoryReader implements Reader {
    private final Path basePath;
    private FileTime lastModifiedTime;

    public DirectoryReader(Path basePath) {
        this.basePath = basePath;
        try {
            if (Files.exists(basePath)) {
                this.lastModifiedTime = Files.getLastModifiedTime(basePath);
            }
        } catch (Exception e) {
            // Ignore initialization errors, will be handled in read()
        }
    }

    @Override
    public String read(String path) throws Exception {
        Path resolvedPath = path != null ? basePath.resolve(path) : basePath;
        if (!Files.exists(resolvedPath)) {
            throw new IllegalArgumentException("Path does not exist: " + resolvedPath);
        }

        byte[] content = Files.readAllBytes(resolvedPath);
        lastModifiedTime = Files.getLastModifiedTime(resolvedPath);
        return new String(content);
    }

    @Override
    public boolean hasChanged() throws Exception {
        if (!Files.exists(basePath) || lastModifiedTime == null) {
            return true;
        }

        FileTime currentModifiedTime = Files.getLastModifiedTime(basePath);
        return !currentModifiedTime.equals(lastModifiedTime);
    }
}
