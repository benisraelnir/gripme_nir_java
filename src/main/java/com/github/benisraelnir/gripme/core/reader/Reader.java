package com.github.benisraelnir.gripme.core.reader;

/**
 * Interface for reading Markdown content from various sources.
 */
public interface Reader {
    /**
     * Reads the content from the source.
     *
     * @param path The path to read from (optional)
     * @return The content as a string
     * @throws Exception if reading fails
     */
    String read(String path) throws Exception;

    /**
     * Checks if the content has been modified since last read.
     *
     * @return true if content has changed, false otherwise
     * @throws Exception if check fails
     */
    boolean hasChanged() throws Exception;
}
