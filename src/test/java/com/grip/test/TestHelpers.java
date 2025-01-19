package com.grip.test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test helper utilities for handling test input/output file operations.
 * Provides functions for constructing file paths and reading contents from test input and output directories.
 */
public class TestHelpers {
    private static final String DIRNAME;
    public static final String USER_CONTEXT = "joeyespo/grip";

    static {
        try {
            DIRNAME = new ClassPathResource("").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize test helpers", e);
        }
    }

    /**
     * Constructs a file path in the input directory.
     *
     * @param parts path components to be joined
     * @return the complete file path
     */
    public static String inputFilename(String... parts) {
        Path path = Paths.get(DIRNAME, "input");
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path.toString();
    }

    /**
     * Constructs a file path in the output directory.
     *
     * @param parts path components to be joined
     * @return the complete file path
     */
    public static String outputFilename(String... parts) {
        Path path = Paths.get(DIRNAME, "output");
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path.toString();
    }

    /**
     * Reads the content of a file from the input directory using UTF-8 encoding.
     *
     * @param parts path components to locate the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String inputFile(String... parts) throws IOException {
        return inputFile(StandardCharsets.UTF_8, parts);
    }

    /**
     * Reads the content of a file from the input directory using the specified encoding.
     *
     * @param encoding the character encoding to use
     * @param parts path components to locate the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String inputFile(Charset encoding, String... parts) throws IOException {
        return new String(Files.readAllBytes(Paths.get(inputFilename(parts))), encoding);
    }

    /**
     * Reads the content of a file from the output directory using UTF-8 encoding.
     *
     * @param parts path components to locate the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String outputFile(String... parts) throws IOException {
        return outputFile(StandardCharsets.UTF_8, parts);
    }

    /**
     * Reads the content of a file from the output directory using the specified encoding.
     *
     * @param encoding the character encoding to use
     * @param parts path components to locate the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String outputFile(Charset encoding, String... parts) throws IOException {
        return new String(Files.readAllBytes(Paths.get(outputFilename(parts))), encoding);
    }

    /**
     * Returns the absolute path to the test resources directory.
     *
     * @return the absolute path to the test resources directory as a string
     */
    public static String getTestResourcesPath() {
        return DIRNAME;
    }
}