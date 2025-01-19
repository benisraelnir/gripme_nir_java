package com.grip.renderer;

import com.grip.vendor.markdown.UrlizeExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

/**
 * Renders the specified Readme locally using flexmark-java.
 * Note: This is currently an incomplete feature.
 * [comment from source project]
 */
public class OfflineRenderer extends ReadmeRenderer {
    private Parser parser;
    private HtmlRenderer renderer;
    private boolean initialized = false;

    /**
     * Constructs an OfflineRenderer with specified settings.
     *
     * @param userContent whether the content is user-generated
     * @param context the context for rendering
     */
    public OfflineRenderer(Boolean userContent, String context) {
        super(userContent, context);
        try {
            MutableDataSet options = new MutableDataSet();
            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TaskListExtension.create(),
                    TablesExtension.create(),
                    TocExtension.create(),
                    UrlizeExtension.create()
            ));
            options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
            options.set(Parser.FENCED_CODE_BLOCK_PARSER, true);
            options.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "highlight-");

            this.parser = Parser.builder(options).build();
            this.renderer = HtmlRenderer.builder(options).build();
            this.initialized = true;
        } catch (NoClassDefFoundError | Exception e) {
            // Handle missing dependencies similar to Python version
            this.initialized = false;
        }
    }

    /**
     * Constructs an OfflineRenderer with default settings.
     */
    public OfflineRenderer() {
        this(null, null);
    }

    /**
     * Renders the specified markdown content and embedded styles.
     *
     * @param text the markdown text to render
     * @param auth authentication credentials (ignored in offline rendering)
     * @return the rendered content
     */
    @Override
    public String render(String text, Object auth) {
        if (text == null) {
            throw new IllegalArgumentException("Expected a string, got null");
        }
        if (!initialized) {
            throw new RuntimeException("Required markdown dependencies not available");
        }
        return renderer.render(parser.parse(text));
    }
}