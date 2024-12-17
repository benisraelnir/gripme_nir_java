package com.github.benisraelnir.gripme.core.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Reads Markdown content from standard input or provided InputStream.
 */
public class StdinReader implements Reader {
    private final InputStream inputStream;
    private String cachedContent;

    public StdinReader() {
        this(System.in);
    }

    public StdinReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String read(String path) throws Exception {
        if (cachedContent == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                cachedContent = reader.lines().collect(Collectors.joining("\n"));
            }
        }
        return cachedContent;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }
}
