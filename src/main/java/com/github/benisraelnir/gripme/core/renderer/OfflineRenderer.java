package com.github.benisraelnir.gripme.core.renderer;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Renders Markdown content using Flexmark library without GitHub API.
 */
@RequiredArgsConstructor
public class OfflineRenderer implements Renderer {
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;

    public OfflineRenderer() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(options));
        this.parser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    @Override
    public String render(String content, Map<String, Object> context) {
        var document = parser.parse(content);
        return htmlRenderer.render(document);
    }

    @Override
    public String renderRaw(String content) throws Exception {
        return render(content, Map.of());
    }
}
