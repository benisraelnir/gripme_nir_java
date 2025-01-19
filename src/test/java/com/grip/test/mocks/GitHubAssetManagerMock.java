package com.grip.test.mocks;

import com.grip.assets.GitHubAssetManager;

import java.util.List;

/**
 * Mock implementation of GitHubAssetManager for testing purposes.
 * This class tracks method calls and simulates asset management operations.
 */
public class GitHubAssetManagerMock extends GitHubAssetManager {
    private int clearCalls = 0;
    private int cacheFilenameCalls = 0;
    private int retrieveStylesCalls = 0;

    public GitHubAssetManagerMock() {
        this(null, null);
    }

    /**
     * Creates a mock GitHubAssetManager with only cache path specified.
     *
     * @param cachePath The path to cache downloaded assets
     */
    public GitHubAssetManagerMock(String cachePath) {
        this(cachePath, null);
    }

    public GitHubAssetManagerMock(String cachePath, List<String> styleUrls) {
        super(cachePath == null ? "dummy-path" : cachePath, styleUrls);
    }

    @Override
    public void clear() {
        clearCalls++;
    }

    @Override
    public String cacheFilename(String url) {
        cacheFilenameCalls++;
        return super.cacheFilename(url);
    }

    @Override
    public void retrieveStyles(String assetUrlPath) {
        retrieveStylesCalls++;
    }

    public int getClearCalls() {
        return clearCalls;
    }

    public int getCacheFilenameCalls() {
        return cacheFilenameCalls;
    }

    public int getRetrieveStylesCalls() {
        return retrieveStylesCalls;
    }
}