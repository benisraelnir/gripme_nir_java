package com.grip.test.mocks;

import com.grip.reader.StdinReader;
import java.io.IOException;

/**
 * Mock implementation of StdinReader for testing purposes.
 * This class simulates stdin input with predefined content.
 */
public class StdinReaderMock extends StdinReader {
    private final String mockStdin;

    /**
     * Creates a new StdinReaderMock with mock stdin content and a display filename.
     *
     * @param mockStdin The content to return when reading stdin
     * @param displayFilename Optional display filename to pass to parent
     */
    public StdinReaderMock(String mockStdin, String displayFilename) {
        super(displayFilename);
        this.mockStdin = mockStdin;
    }

    /**
     * Creates a new StdinReaderMock with mock stdin content.
     *
     * @param mockStdin The content to return when reading stdin
     */
    public StdinReaderMock(String mockStdin) {
        this(mockStdin, null);
    }

    /**
     * Protected method to provide mock stdin content.
     * This is called by the parent class's read method.
     */
    protected String readStdin() throws IOException {
        return mockStdin;
    }
}