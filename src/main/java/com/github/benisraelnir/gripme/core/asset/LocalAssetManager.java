package com.github.benisraelnir.gripme.core.asset;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Manages static assets from the local classpath.
 */
public class LocalAssetManager implements AssetManager {
    private static final String ASSETS_PATH = "static";

    @Override
    public Optional<byte[]> getAsset(Path path) throws Exception {
        try {
            var resource = new ClassPathResource(ASSETS_PATH + "/" + path.toString());
            if (resource.exists()) {
                return Optional.of(resource.getInputStream().readAllBytes());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasAsset(Path path) {
        try {
            var resource = new ClassPathResource(ASSETS_PATH + "/" + path.toString());
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getMimeType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (Exception e) {
            // Default to octet-stream for unknown types
            return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
