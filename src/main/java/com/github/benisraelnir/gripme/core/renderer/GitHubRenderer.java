package com.github.benisraelnir.gripme.core.renderer;

import com.github.benisraelnir.gripme.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Renders Markdown content using GitHub's API.
 */
@Component
@RequiredArgsConstructor
public class GitHubRenderer implements Renderer {
    private final GitHubService githubService;

    @Override
    public String render(String content, Map<String, Object> context) throws Exception {
        boolean isGfm = (boolean) context.getOrDefault("gfm", false);
        String repoContext = (String) context.getOrDefault("context", "");
        return githubService.renderMarkdown(content, isGfm, repoContext);
    }

    @Override
    public String renderRaw(String content) throws Exception {
        return render(content, Map.of());
    }
}
