package com.github.benisraelnir.gripme.core.asset;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages static assets from GitHub's CDN.
 */
@RequiredArgsConstructor
public class GitHubAssetManager implements AssetManager {
    private final RestTemplate restTemplate;
    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();
    private final String githubStyleUrl;

    @Override
    public Optional<byte[]> getAsset(Path path) throws Exception {
        String assetPath = path.toString();
        if (cache.containsKey(assetPath)) {
            return Optional.of(cache.get(assetPath));
        }

        try {
            byte[] content = restTemplate.getForObject(
                githubStyleUrl + "/" + assetPath,
                byte[].class
            );
            if (content != null) {
                cache.put(assetPath, content);
                return Optional.of(content);
            }
        } catch (Exception e) {
            // Asset not found or error fetching
        }
        return Optional.empty();
    }

    @Override
    public boolean hasAsset(Path path) {
        return cache.containsKey(path.toString());
    }

    @Override
    public String getMimeType(Path path) {
        String fileName = path.toString().toLowerCase();
        if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
    }
}
