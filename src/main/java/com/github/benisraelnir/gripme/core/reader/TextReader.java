package com.github.benisraelnir.gripme.core.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Reads Markdown content from a text string.
 */
@Component
@RequiredArgsConstructor
public class TextReader implements Reader {
    private final String content;

    @Override
    public String read(String path) {
        return content;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }
}
