package com.github.benisraelnir.gripme.core.reader;

/**
 * Reads Markdown content from a text string.
 */
public class TextReader implements Reader {
    private final String content;
    private final long creationTime;

    public TextReader(String content) {
        this.content = content;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public String read(String path) {
        return content;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public Long lastUpdated(String path) {
        return creationTime;
    }
}
