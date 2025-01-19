package com.grip.api;

import java.util.Arrays;

/**
 * Represents authentication credentials for GitHub API access.
 * This class handles secure password storage using char arrays that are cleared after use.
 */
public class AuthCredentials implements AutoCloseable {
    private final String username;
    private final char[] password;

    /**
     * Creates a new AuthCredentials instance.
     * The password array will be cloned for security.
     *
     * @param username GitHub username
     * @param password GitHub password or token as char array (will be cloned)
     */
    public AuthCredentials(String username, char[] password) {
        this.username = username;
        this.password = password != null ? password.clone() : null;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets a copy of the password array.
     * The caller is responsible for clearing the returned array.
     *
     * @return a copy of the password array, or null if no password set
     */
    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    /**
     * Creates an AuthCredentials instance from the given username and password.
     * The password array will be cloned for security.
     *
     * @param username GitHub username
     * @param password GitHub password or token as char array (will be cloned)
     * @return new AuthCredentials instance, or null if both username and password are null
     */
    public static AuthCredentials from(String username, char[] password) {
        if (username == null && password == null) {
            return null;
        }
        return new AuthCredentials(username, password);
    }

    /**
     * Clears the password array for security.
     */
    @Override
    public void close() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}