package com.github.benisraelnir.gripme.core.asset;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface for managing static assets (CSS, images, etc.).
 */
public interface AssetManager {
    /**
     * Gets the content of an asset by its path.
     *
     * @param path The path to the asset
     * @return Optional containing the asset content if found
     * @throws Exception if retrieval fails
     */
    Optional<byte[]> getAsset(Path path) throws Exception;

    /**
     * Checks if an asset exists at the given path.
     *
     * @param path The path to check
     * @return true if asset exists, false otherwise
     */
    boolean hasAsset(Path path);

    /**
     * Gets the MIME type for an asset.
     *
     * @param path The path to the asset
     * @return The MIME type string
     */
    String getMimeType(Path path);
}
