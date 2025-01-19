package com.grip.assets;

import com.grip.util.PathUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the style and font assets rendered with Readme pages.
 * Set cachePath to null to disable caching.
 */
public abstract class ReadmeAssetManager {
    protected final String cachePath;
    protected final List<String> styleUrls;
    protected final List<String> styles;
    protected final boolean quiet;

    /**
     * Returns the list of style URLs.
     */
    public List<String> getStyleUrls() {
        return styleUrls;
    }

    /**
     * Returns the list of styles.
     */
    public List<String> getStyles() {
        return styles;
    }

    /**
     * Returns the cache path.
     */
    public String getCachePath() {
        return cachePath;
    }

    /**
     * Creates a new ReadmeAssetManager instance.
     *
     * @param cachePath  The path to cache downloaded assets, or null to disable caching
     * @param styleUrls  List of style URLs to use, or null for default
     * @param quiet      Whether to suppress output messages
     */
    protected ReadmeAssetManager() {
        this(null, null, null);
    }

    protected ReadmeAssetManager(String cachePath, List<String> styleUrls, Boolean quiet) {
        this.cachePath = cachePath;
        this.styleUrls = new ArrayList<>(styleUrls != null ? styleUrls : new ArrayList<>());
        this.styles = new ArrayList<>();
        this.quiet = quiet != null && quiet;
    }

    /**
     * Gets the cache path for assets.
     *
     * @return The cache path, or null if caching is disabled
     */
    

    /**
     * Gets the full path for a specific asset.
     *
     * @param assetPath The relative asset path
     * @return The full path to the asset
     */
    public String getAssetPath(String assetPath) {
        if (cachePath == null) {
            return assetPath;
        }
        return Paths.get(cachePath, assetPath).toString();
    }

    /**
     * Strips URL parameters and fragments from a URL.
     *
     * @param url The URL to strip
     * @return The URL without parameters or fragments
     */
    protected String stripUrlParams(String url) {
        String result = url;
        int paramIndex = result.lastIndexOf('?');
        if (paramIndex != -1) {
            result = result.substring(0, paramIndex);
        }
        int fragmentIndex = result.lastIndexOf('#');
        if (fragmentIndex != -1) {
            result = result.substring(0, fragmentIndex);
        }
        return result;
    }

    /**
     * Clears the asset cache.
     */
    public void clear() {
        if (cachePath != null && Files.exists(Paths.get(cachePath))) {
            try {
                Files.walk(Paths.get(cachePath))
                        .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete contents first
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                if (!quiet) {
                                    System.err.println("Warning: Failed to delete " + path + ": " + e.getMessage());
                                }
                            }
                        });
            } catch (IOException e) {
                if (!quiet) {
                    System.err.println("Warning: Failed to clear cache: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gets a suitable relative filename for the specified URL.
     *
     * @param url The URL to get a filename for
     * @return The filename to use for the URL
     */
    protected String cacheFilename(String url) {
        Path path = Paths.get(url);
        return stripUrlParams(path.getFileName().toString());
    }

    /**
     * Get style URLs from the source HTML page and specified cached asset URL path.
     * This method must be implemented by subclasses.
     *
     * @param assetUrlPath The base URL path for assets
     */
    public abstract void retrieveStyles(String assetUrlPath);
}