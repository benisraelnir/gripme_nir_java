package com.github.benisraelnir.gripme.core.renderer;

import java.util.Map;

/**
 * Interface for rendering Markdown content to HTML.
 */
public interface Renderer {
    /**
     * Renders Markdown content to HTML.
     *
     * @param content The Markdown content to render
     * @param context Additional context for rendering (e.g., GitHub context)
     * @return The rendered HTML
     * @throws Exception if rendering fails
     */
    String render(String content, Map<String, Object> context) throws Exception;
}
