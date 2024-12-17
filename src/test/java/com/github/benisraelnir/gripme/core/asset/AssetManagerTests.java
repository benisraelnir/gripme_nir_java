package com.github.benisraelnir.gripme.core.asset;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetManagerTests {
    @Test
    void localAssetManagerShouldHandleNonExistentAsset() {
        AssetManager manager = new LocalAssetManager();
        Path nonExistentPath = Path.of("non-existent.css");

        assertDoesNotThrow(() -> {
            Optional<byte[]> result = manager.getAsset(nonExistentPath);
            assertFalse(result.isPresent());
            assertFalse(manager.hasAsset(nonExistentPath));
        });
    }

    @Test
    void localAssetManagerShouldDetectMimeTypes() {
        AssetManager manager = new LocalAssetManager();

        assertEquals("text/css", manager.getMimeType(Path.of("style.css")));
        assertEquals("image/png", manager.getMimeType(Path.of("image.png")));
    }

    @Test
    void githubAssetManagerShouldCacheAssets() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String githubStyleUrl = "https://github.com/style";
        AssetManager manager = new GitHubAssetManager(restTemplate, githubStyleUrl);
        Path assetPath = Path.of("style.css");
        byte[] assetContent = "body { color: black; }".getBytes();

        when(restTemplate.getForObject(
            eq(githubStyleUrl + "/" + assetPath),
            eq(byte[].class)
        )).thenReturn(assetContent);

        Optional<byte[]> firstResult = manager.getAsset(assetPath);
        assertTrue(firstResult.isPresent());
        assertArrayEquals(assetContent, firstResult.get());

        // Second request should use cached content
        Optional<byte[]> secondResult = manager.getAsset(assetPath);
        assertTrue(secondResult.isPresent());
        assertArrayEquals(assetContent, secondResult.get());
        assertTrue(manager.hasAsset(assetPath));
    }

    @Test
    void githubAssetManagerShouldHandleFailedRequests() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String githubStyleUrl = "https://github.com/style";
        AssetManager manager = new GitHubAssetManager(restTemplate, githubStyleUrl);
        Path assetPath = Path.of("non-existent.css");

        when(restTemplate.getForObject(
            eq(githubStyleUrl + "/" + assetPath),
            eq(byte[].class)
        )).thenReturn(null);

        Optional<byte[]> result = manager.getAsset(assetPath);
        assertFalse(result.isPresent());
        assertFalse(manager.hasAsset(assetPath));
    }
}
