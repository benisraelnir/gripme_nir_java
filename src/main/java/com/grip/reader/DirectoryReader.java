package com.grip.reader;

import com.grip.config.GripConstants;
import com.grip.exceptions.ReadmeNotFoundException;
import com.grip.util.PathUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.ArrayList;

/**
 * Reads Readme files from URL subpaths.
 */
public class DirectoryReader extends ReadmeReader {
    private final Path rootFilename;
    private final Path rootDirectory;

    /**
     * Returns the absolute path to the root README file.
     *
     * @return String representation of the root README file path
     */
    public String getRootFilename() {
        return rootFilename.toString();
    }

    /**
     * Returns the absolute path to the base directory.
     *
     * @return String representation of the root directory path
     */
    public String getRootDirectory() {
        return rootDirectory.toString();
    }

    /**
     * Creates a new DirectoryReader.
     *
     * @param path The path to the README file or directory
     * @param silent If true, returns default filename instead of throwing exceptions
     * @throws ReadmeNotFoundException if the README file cannot be found and silent is false
     */
    public DirectoryReader(String path, boolean silent) throws ReadmeNotFoundException {
        Path resolvedPath = resolveReadme(path, silent);
        this.rootFilename = resolvedPath.toAbsolutePath().normalize();
        this.rootDirectory = rootFilename.getParent();
    }

    public DirectoryReader(String path) throws ReadmeNotFoundException {
        this(path, false);
    }

    public DirectoryReader() throws ReadmeNotFoundException {
        this(null);
    }

    private Path findFile(String path) throws ReadmeNotFoundException {
        return findFile(path, false);
    }

    private Path findFile(String path, boolean silent) throws ReadmeNotFoundException {
        Path directory = path != null ? Paths.get(path) : Paths.get(".");
        
        for (String filename : GripConstants.DEFAULT_FILENAMES) {
            Path fullPath = directory.resolve(filename);
            if (Files.exists(fullPath)) {
                return fullPath;
            }
        }

        // Return default filename if silent
        if (silent) {
            return directory.resolve(GripConstants.DEFAULT_FILENAME);
        }

        throw new ReadmeNotFoundException(directory.toString());
    }

    private Path resolveReadme(String path) throws ReadmeNotFoundException {
        return resolveReadme(path, false);
    }

    private Path resolveReadme(String path, boolean silent) throws ReadmeNotFoundException {
        try {
            // Default to current working directory
            if (path == null) {
                path = ".";
            }

            // Normalize the path
            Path normalizedPath = Paths.get(path).normalize();

            try {
                // Resolve README file if path is a directory
                if (Files.isDirectory(normalizedPath)) {
                    return findFile(normalizedPath.toString(), silent);
                }

                // Return path if file exists or if silent
                if (silent || Files.exists(normalizedPath)) {
                    return normalizedPath;
                }
            } catch (SecurityException e) {
                throw new ReadmeNotFoundException(path, "Access denied: " + path);
            }

            throw new ReadmeNotFoundException(path, "File not found: " + path);
        } catch (SecurityException e) {
            throw new ReadmeNotFoundException(path, "Access denied: " + path);
        }
    }

    private String readText(Path filename) throws IOException {
        try {
            return Files.readString(filename, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ReadmeNotFoundException(filename.toString());
        }
    }

    private byte[] readBinary(Path filename) throws IOException {
        try {
            return Files.readAllBytes(filename);
        } catch (NoSuchFileException e) {
            throw new ReadmeNotFoundException(filename.toString());
        }
    }

    @Override
    public String normalizeSubpath(String subpath) {
        if (subpath == null) {
            return null;
        }
        
        // Special cases to match Python behavior
        if (subpath.equals(".") || subpath.equals("./") || 
            subpath.matches("\\./+") || subpath.matches(".*?/\\.\\./\\./")) {
            return "./";
        }
        
        // Remove trailing slashes except for root
        String normalized = subpath;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        // Normalize path segments
        String[] parts = normalized.split("/");
        List<String> normalizedParts = new ArrayList<>();
        for (String part : parts) {
            if (part.equals(".") || part.isEmpty()) {
                continue;
            } else if (part.equals("..")) {
                if (!normalizedParts.isEmpty() && !normalizedParts.get(normalizedParts.size() - 1).equals("..")) {
                    normalizedParts.remove(normalizedParts.size() - 1);
                } else {
                    normalizedParts.add("..");
                }
            } else {
                normalizedParts.add(part);
            }
        }
        
        // Handle empty result
        if (normalizedParts.isEmpty()) {
            return "./";
        }
        
        // Reconstruct path
        normalized = String.join("/", normalizedParts);
        
        try {
            String fullPath = PathUtils.safeJoin(rootDirectory.toString(), normalized);
            Path filename = Paths.get(fullPath);
            
            if (Files.isDirectory(filename)) {
                normalized += "/";
            }
            return normalized;
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Path would fall outside the root directory: " + subpath);
        }
    }

    public Path readmeFor(String subpath) throws ReadmeNotFoundException {
        System.out.println("[DEBUG] DirectoryReader.readmeFor: subpath=" + subpath);
        System.out.println("[DEBUG] DirectoryReader.readmeFor: rootDirectory=" + rootDirectory);
        System.out.println("[DEBUG] DirectoryReader.readmeFor: rootFilename=" + rootFilename);
        
        if (subpath == null) {
            return rootFilename;
        }

        // Join for safety and to convert subpath to normalized OS-specific path
        String safePath = PathUtils.safeJoin(rootDirectory.toString(), subpath);
        System.out.println("[DEBUG] DirectoryReader.readmeFor: safePath=" + safePath);
        
        Path filename = Paths.get(safePath).normalize();
        System.out.println("[DEBUG] DirectoryReader.readmeFor: filename=" + filename);

        // Check for existence
        if (!Files.exists(filename)) {
            System.out.println("[DEBUG] DirectoryReader.readmeFor: file does not exist");
            throw new ReadmeNotFoundException(filename.toString());
        }

        // Resolve README file if path is a directory
        if (Files.isDirectory(filename)) {
            System.out.println("[DEBUG] DirectoryReader.readmeFor: is directory");
            return findFile(filename.toString(), false);
        }

        System.out.println("[DEBUG] DirectoryReader.readmeFor: returning filename=" + filename);
        return filename;
    }

    @Override
    public String filenameFor(String subpath) {
        try {
            Path filename = readmeFor(subpath);
            return rootDirectory.relativize(filename).toString().replace('\\', '/');
        } catch (ReadmeNotFoundException e) {
            return null;
        }
    }

    @Override
    public Long lastUpdated(String subpath) {
        try {
            return Files.getLastModifiedTime(readmeFor(subpath)).toMillis();
        } catch (ReadmeNotFoundException e) {
            return null;
        } catch (NoSuchFileException e) {
            // Equivalent to Python's errno.ENOENT
            return null;
        } catch (IOException e) {
            // Re-throw other IO exceptions as in Python
            throw new RuntimeException(e);
        }
    }

    @Override
    public String mimetypeFor(String subpath) {
        try {
            Path filename = readmeFor(subpath);
            String name = filename.getFileName().toString();
            return java.net.URLConnection.guessContentTypeFromName(name);
        } catch (ReadmeNotFoundException e) {
            return super.mimetypeFor(subpath);
        }
    }

    @Override
    public Object read(String subpath) throws IOException {
        try {
            boolean isBinary = isBinary(subpath);
            Path filename = readmeFor(subpath);
            return isBinary ? readBinary(filename) : readText(filename);
        } catch (NoSuchFileException e) {
            throw new ReadmeNotFoundException(e.getFile());
        } catch (SecurityException | AccessDeniedException e) {
            throw new ReadmeNotFoundException(subpath, "Access denied");
        } catch (IOException e) {
            // Handle other IO errors (equivalent to Python's EnvironmentError)
            throw new ReadmeNotFoundException(subpath, e.getMessage());
        }
    }
}