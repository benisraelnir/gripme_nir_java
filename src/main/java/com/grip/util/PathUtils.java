package com.grip.util;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;

/**
 * Utility class providing safe file path operations.
 * This class is a port of the Python grip._compat module's safe_join functionality.
 */
public final class PathUtils {
    
    private PathUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Safely join zero or more untrusted path components to a base directory to avoid
     * escaping the base directory.
     *
     * @param directory The trusted base directory
     * @param pathnames The untrusted path components relative to the base directory
     * @return A safe path string
     * @throws ResponseStatusException with NOT_FOUND status if the path is invalid or outside base directory
     */
    public static String safeJoin(String directory, String... pathnames) {
        try {
            Path basePath = Paths.get(directory).toAbsolutePath().normalize();
            
            if (pathnames.length == 0) {
                return basePath.toString();
            }

            Path result = basePath;
            for (String pathname : pathnames) {
                // Resolve each path component separately to catch any attempts to escape
                result = result.resolve(pathname).normalize();
            }

            // Verify the resulting path is still within the base directory and is not null
            if (result == null || !result.startsWith(basePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid path");
            }

            return result.toString();
        } catch (InvalidPathException | SecurityException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid path", e);
        }
    }
}