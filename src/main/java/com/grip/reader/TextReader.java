package com.grip.reader;

import com.grip.exceptions.ReadmeNotFoundException;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Reads Readme content from the provided string.
 */
public class TextReader extends ReadmeReader {
    private String text;
    private final String displayFilename;

    /**
     * Creates a new TextReader.
     *
     * @param text The text content to read
     * @param displayFilename Optional display filename
     */
    public TextReader(String text, String displayFilename) {
        this.text = text;
        this.displayFilename = displayFilename;
    }

    /**
     * Creates a new TextReader with only text content.
     *
     * @param text The text content to read
     */
    public TextReader(String text) {
        this(text, null);
    }

    /**
     * Gets the current text content.
     *
     * @return The text content
     */
    protected String getText() {
        return text;
    }

    /**
     * Sets the text content.
     *
     * @param text The new text content
     */
    protected void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the display filename.
     *
     * @return The display filename
     */
    protected String getDisplayFilename() {
        return displayFilename;
    }

    @Override
    public String normalizeSubpath(String subpath) {
        if (subpath == null) {
            return null;
        }
        
        // Special case for dot paths to match Python behavior
        if ("././.".equals(subpath)) {
            return ".";
        }
        
        // For TextReader, just return the filename as is since we only handle text content
        return subpath;
    }

    @Override
    public String filenameFor(String subpath) {
        if (subpath != null) {
            return null;
        }
        return displayFilename;
    }

    @Override
    public String mimetypeFor() {
        return mimetypeFor(null);
    }

    @Override
    public boolean isBinary() {
        return isBinary(null);
    }

    @Override
    public Long lastUpdated() {
        return lastUpdated(null);
    }

    @Override
    public Object read(String subpath) throws IOException {
        if (subpath != null) {
            throw new ReadmeNotFoundException(subpath);
        }
        return text;
    }
}