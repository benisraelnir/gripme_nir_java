package com.github.benisraelnir.gripme.core.reader;

import lombok.RequiredArgsConstructor;

/**
 * Reads Markdown content from a text string.
 */
@RequiredArgsConstructor
public class TextReader implements Reader {
    private final String content;

    @Override
    public String read() {
        return content;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }
}
