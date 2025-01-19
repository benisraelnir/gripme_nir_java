package com.grip.reader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads Readme content from a URL subpath.
 */
public abstract class ReadmeReader {

    /**
     * Returns the normalized subpath.
     * 
     * This allows Readme files to be inferred from directories while
     * still allowing relative paths to work properly.
     * 
     * Override to change the default behavior of returning the
     * specified subpath as-is.
     *
     * @param subpath The subpath to normalize
     * @return The normalized subpath, or null if subpath is null
     */
    public String normalizeSubpath(String subpath) {
        if (subpath == null) {
            return null;
        }
        return Paths.get(subpath).normalize().toString().replace('\\', '/');
    }

    /**
     * Returns the relative filename for the specified subpath, or null
     * if the file does not exist.
     *
     * @param subpath The subpath to get the filename for
     * @return The relative filename, or null if the file does not exist
     */
    public String filenameFor(String subpath) {
        return null;
    }

    /**
     * Returns the relative filename using the default subpath.
     *
     * @return The relative filename, or null if the file does not exist
     */
    public String filenameFor() {
        return filenameFor(null);
    }

    /**
     * Gets the mimetype for the specified subpath.
     *
     * @param subpath The subpath to get the mimetype for, defaults to DEFAULT_FILENAME if null
     * @return The mimetype, or null if it cannot be determined
     */
    public String mimetypeFor(String subpath) {
        if (subpath == null) {
            subpath = com.grip.config.GripConstants.DEFAULT_FILENAME;
        }
        return java.net.URLConnection.guessContentTypeFromName(subpath);
    }

    /**
     * Gets whether the specified subpath is a supported binary file.
     *
     * @param subpath The subpath to check
     * @return true if the file is binary, false otherwise
     */
    public boolean isBinary(String subpath) {
        String mimetype = mimetypeFor(subpath);
        return mimetype != null && !mimetype.startsWith("text/");
    }

    /**
     * Returns the time of the last modification of the Readme or
     * specified subpath. null is returned if the reader doesn't
     * support modification tracking.
     *
     * The return value is a number giving the number of milliseconds since
     * the epoch (see System.currentTimeMillis()).
     *
     * @param subpath The subpath to check
     * @return The last modified time in milliseconds, or null if not supported
     */
    public Long lastUpdated(String subpath) {
        return null;
    }

    /**
     * Returns the content of the specified subpath.
     *
     * @param subpath The subpath to read, expected to already have been normalized
     * @return The content as a String or byte array for binary files
     * @throws IOException if an I/O error occurs
     */
    public abstract Object read(String subpath) throws IOException;

    /**
     * Returns the content using the default subpath.
     *
     * @return The content as a String or byte array for binary files
     * @throws IOException if an I/O error occurs
     */
    public Object read() throws IOException {
        return read(null);
    }

    /**
     * Gets the mimetype using the default subpath.
     *
     * @return The mimetype, or null if it cannot be determined
     */
    public String mimetypeFor() {
        return mimetypeFor(null);
    }

    /**
     * Gets whether the default subpath is a supported binary file.
     *
     * @return true if the file is binary, false otherwise
     */
    public boolean isBinary() {
        return isBinary(null);
    }

    /**
     * Returns the time of the last modification using the default subpath.
     *
     * @return The last modified time in milliseconds, or null if not supported
     */
    public Long lastUpdated() {
        return lastUpdated(null);
    }
}