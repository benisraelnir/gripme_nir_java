package com.github.benisraelnir.gripme.core.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads Markdown content from a directory/file path.
 */
@Component
public class DirectoryReader implements Reader {
    private final Path basePath;
    private final Map<Path, FileTime> lastModifiedTimes;

    public DirectoryReader(Path basePath) {
        this.basePath = basePath;
        this.lastModifiedTimes = new HashMap<>();
        try {
            if (Files.exists(basePath)) {
                this.lastModifiedTimes.put(basePath, Files.getLastModifiedTime(basePath));
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
        lastModifiedTimes.put(resolvedPath, Files.getLastModifiedTime(resolvedPath));
        return new String(content);
    }

    @Override
    public boolean hasChanged() throws Exception {
        for (Map.Entry<Path, FileTime> entry : lastModifiedTimes.entrySet()) {
            Path path = entry.getKey();
            FileTime lastModifiedTime = entry.getValue();

            if (!Files.exists(path)) {
                return true;
            }

            FileTime currentModifiedTime = Files.getLastModifiedTime(path);
            if (!currentModifiedTime.equals(lastModifiedTime)) {
                return true;
            }
        }
        return false;
    }
}
