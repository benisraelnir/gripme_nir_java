package com.grip.exceptions;

import java.io.FileNotFoundException;

/**
 * Exception thrown when a README file cannot be found.
 * Java equivalent of Python's ReadmeNotFoundError.
 */
public class ReadmeNotFoundException extends FileNotFoundException {
    private static final long serialVersionUID = 1L;
    
    private final String path;
    private final String customMessage;

    /**
     * Constructs a new ReadmeNotFoundException with no path or message.
     */
    public ReadmeNotFoundException() {
        super("README not found");
        this.path = null;
        this.customMessage = null;
    }

    /**
     * Constructs a new ReadmeNotFoundException with the specified path.
     *
     * @param path the path where README was not found
     */
    public ReadmeNotFoundException(String path) {
        super("README not found: " + path);
        this.path = path;
        this.customMessage = null;
    }

    /**
     * Constructs a new ReadmeNotFoundException with the specified path and message.
     *
     * @param path the path where README was not found
     * @param message custom error message
     */
    public ReadmeNotFoundException(String path, String message) {
        super(message != null ? message : "README not found: " + path);
        this.path = path;
        this.customMessage = message;
    }

    /**
     * Gets the path where README was not found.
     *
     * @return the path, or null if not specified
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the custom message if specified.
     *
     * @return the custom message, or null if not specified
     */
    public String getCustomMessage() {
        return customMessage;
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", 
            getClass().getSimpleName(),
            path != null ? "\"" + path + "\"" : "null",
            customMessage != null ? "\"" + customMessage + "\"" : "null");
    }

    @Override
    public String getMessage() {
        if (customMessage != null) {
            return customMessage;
        }

        if (path != null) {
            return "No README found at " + path;
        }

        return "README not found";
    }
}