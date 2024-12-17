package com.github.benisraelnir.gripme.core.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Reads Markdown content from standard input.
 */
public class StdinReader implements Reader {
    private String cachedContent;

    @Override
    public String read() throws Exception {
        if (cachedContent == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
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
