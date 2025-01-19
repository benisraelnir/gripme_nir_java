package com.grip.test.mocks;

import com.grip.assets.GitHubAssetManager;
import com.grip.assets.ReadmeAssetManager;
import com.grip.core.Grip;
import com.grip.exceptions.ReadmeNotFoundException;
import com.grip.reader.DirectoryReader;
import com.grip.reader.ReadmeReader;
import com.grip.renderer.ReadmeRenderer;
import java.io.IOException;

/**
 * Mock implementation of Grip for testing purposes.
 * This class provides a custom asset manager for testing and
 * overrides DirectoryReader to avoid README file requirement.
 */
public class GripMock extends Grip {

    private static final ReadmeReader MOCK_READER = new ReadmeReader() {
        @Override
        public Object read(String subpath) throws IOException {
            return "# Mock README\nThis is a mock readme for testing.";
        }

        @Override
        public String normalizeSubpath(String subpath) {
            return subpath;
        }

        @Override
        public String filenameFor(String subpath) {
            return "README.md";
        }
    };

    /**
     * Creates a new GripMock instance with a mock DirectoryReader
     * that doesn't require a real README file.
     */
    public GripMock() throws ReadmeNotFoundException {
        super(MOCK_READER);
    }

    /**
     * Creates a new GripMock instance with a mock DirectoryReader and custom renderer.
     *
     * @param auth The authentication object
     * @param renderer The renderer to use
     */
    public GripMock(Object auth, ReadmeRenderer renderer) throws ReadmeNotFoundException {
        super(MOCK_READER, auth, renderer);
    }

    /**
     * Creates a new GripMock instance with a file path, auth, and custom renderer.
     *
     * @param filePath The path to the file to render
     * @param auth The authentication object
     * @param renderer The renderer to use
     */
    public GripMock(String filePath, Object auth, ReadmeRenderer renderer) throws ReadmeNotFoundException {
        super(new DirectoryReader(filePath), auth, renderer);
    }

    @Override
    protected GitHubAssetManager defaultAssetManager() {
        return new GitHubAssetManagerMock();
    }
}