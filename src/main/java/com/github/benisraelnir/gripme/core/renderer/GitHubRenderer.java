package com.github.benisraelnir.gripme.core.renderer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Renders Markdown content using GitHub's API.
 */
@RequiredArgsConstructor
public class GitHubRenderer implements Renderer {
    private final RestTemplate restTemplate;
    private final String githubApiUrl;
    private final Map<String, String> headers;

    @Override
    public String render(String content, Map<String, Object> context) throws Exception {
        var request = Map.of(
            "text", content,
            "mode", "markdown",
            "context", context.getOrDefault("context", "")
        );

        return restTemplate.postForObject(
            githubApiUrl + "/markdown",
            request,
            String.class
        );
    }
}
