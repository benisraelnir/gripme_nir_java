package com.grip.assets;

import com.grip.config.GripConstants;
import com.grip.util.PathUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the styles used for rendering Readme pages.
 * Set cachePath to null to disable caching.
 */
public class GitHubAssetManager extends ReadmeAssetManager {

    /**
     * Creates a new GitHubAssetManager instance.
     *
     * @param cachePath  The path to cache downloaded assets, or null to disable caching
     * @param styleUrls  List of style URLs to use, or null for default
     * @param quiet      Whether to suppress output messages
     */
    public GitHubAssetManager(String cachePath) {
        this(cachePath, null, null);
    }

    public GitHubAssetManager(String cachePath, List<String> styleUrls) {
        this(cachePath, styleUrls, null);
    }

    public GitHubAssetManager(String cachePath, List<String> styleUrls, Boolean quiet) {
        super(cachePath, styleUrls, quiet);
    }

    private List<String> getStyleUrls(String assetUrlPath) {
        // Check cache first
        if (cachePath != null) {
            List<String> cached = getCachedStyleUrls(assetUrlPath);
            if (!cached.isEmpty()) {
                return cached;
            }
        }

        // Find style URLs
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(GripConstants.STYLE_URLS_SOURCE);
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    if (!quiet) {
                        System.err.println("Warning: retrieving styles gave status code " + statusCode);
                    }
                }

                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                List<String> urls = new ArrayList<>();
                
                for (String styleUrlsRe : GripConstants.STYLE_URLS_RES) {
                    Pattern pattern = Pattern.compile(styleUrlsRe);
                    Matcher matcher = pattern.matcher(content);
                    List<String> matches = new ArrayList<>();
                    while (matcher.find()) {
                        String match = matcher.group(1);
                        matches.add(match);
                        urls.add(match);
                    }
                    if (!quiet) {
                        System.err.println(matches);
                    }
                }

                if (urls.isEmpty() && !quiet) {
                    System.err.println("Warning: no styles found - see https://github.com/joeyespo/grip/issues/265");
                }

                // Cache the styles and their assets
                if (cachePath != null) {
                    boolean isCached = cacheContents(urls, assetUrlPath);
                    if (isCached) {
                        urls = getCachedStyleUrls(assetUrlPath);
                    }
                }

                return urls;
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to retrieve styles: " + e.getMessage(), e);
        }
    }

    private List<String> getCachedStyleUrls(String assetUrlPath) {
        if (cachePath == null) {
            return new ArrayList<>();
        }

        try {
            List<String> cachedStyles = new ArrayList<>();
            Files.list(Paths.get(cachePath))
                    .filter(path -> path.toString().endsWith(".css"))
                    .forEach(path -> cachedStyles.add(
                            assetUrlPath + "/" + path.getFileName().toString()));
            return cachedStyles;
        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory")) {
                return new ArrayList<>();
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get cached styles: " + e.getMessage(), e);
        }
    }

    private boolean cacheContents(List<String> styleUrls, String assetUrlPath) {
        Map<String, byte[]> files = new HashMap<>();
        List<String> assetUrls = new ArrayList<>();

        // Download and process style files
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (String styleUrl : styleUrls) {
                if (!quiet) {
                    System.err.println(" * Downloading style " + styleUrl);
                }

                HttpGet request = new HttpGet(styleUrl);
                try (CloseableHttpResponse response = client.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode >= 300) {
                        if (!quiet) {
                            System.err.println(" -> Warning: Style request responded with " + statusCode);
                        }
                        files = null;
                        continue;
                    }

                    String assetContent = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                    // Find assets and replace their base URLs with the cache directory
                    Pattern pattern = Pattern.compile(GripConstants.STYLE_ASSET_URLS_RE);
                    Matcher matcher = pattern.matcher(assetContent);
                    while (matcher.find()) {
                        String assetUrl = new URL(new URL(styleUrl), matcher.group(1)).toString();
                        assetUrls.add(assetUrl);
                    }

                    String contents = assetContent.replaceAll(
                            GripConstants.STYLE_ASSET_URLS_RE,
                            String.format(GripConstants.STYLE_ASSET_URLS_SUB_FORMAT, 
                                    assetUrlPath.replaceAll("/$", "")));

                    if (files != null) {
                        String filename = cacheFilename(styleUrl);
                        files.put(filename, contents.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            // Download asset files
            for (String assetUrl : assetUrls) {
                if (!quiet) {
                    System.err.println(" * Downloading asset " + assetUrl);
                }

                HttpGet request = new HttpGet(assetUrl);
                try (CloseableHttpResponse response = client.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode >= 300) {
                        if (!quiet) {
                            System.err.println(" -> Warning: Asset request responded with " + statusCode);
                        }
                        files = null;
                        continue;
                    }

                    if (files != null) {
                        String filename = cacheFilename(assetUrl);
                        HttpEntity entity = response.getEntity();
                        try (InputStream content = entity.getContent()) {
                            files.put(filename, content.readAllBytes());
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to cache contents: " + e.getMessage(), e);
        }

        // Skip caching if something went wrong
        if (files == null || files.isEmpty()) {
            return false;
        }

        // Cache files if all downloads were successful
        try {
            Map<String, byte[]> cache = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                cache.put(PathUtils.safeJoin(cachePath, entry.getKey()), entry.getValue());
            }

            Files.createDirectories(Paths.get(cachePath));
            for (Map.Entry<String, byte[]> entry : cache.entrySet()) {
                Files.write(Paths.get(entry.getKey()), entry.getValue());
            }

            if (!quiet) {
                System.err.println(" * Cached all downloads in " + cachePath);
            }
            return true;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to write cache: " + e.getMessage(), e);
        }
    }

    @Override
    public void retrieveStyles(String assetUrlPath) {
        if (!assetUrlPath.endsWith("/")) {
            assetUrlPath += "/";
        }
        List<String> newUrls = getStyleUrls(assetUrlPath);
        List<String> combinedUrls = new ArrayList<>(styleUrls);
        combinedUrls.addAll(newUrls);
        styleUrls.clear();
        styleUrls.addAll(combinedUrls);
    }
}