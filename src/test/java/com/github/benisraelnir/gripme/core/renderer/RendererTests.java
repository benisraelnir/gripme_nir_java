package com.github.benisraelnir.gripme.core.renderer;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RendererTests {
    @Test
    void offlineRendererShouldRenderMarkdown() {
        Renderer renderer = new OfflineRenderer();
        String markdown = "# Test Header\nTest paragraph";
        Map<String, Object> context = new HashMap<>();

        String result = assertDoesNotThrow(() -> renderer.render(markdown, context));

        assertTrue(result.contains("<h1>Test Header</h1>"));
        assertTrue(result.contains("<p>Test paragraph</p>"));
    }

    @Test
    void githubRendererShouldCallGithubApi() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        String githubApiUrl = "https://api.github.com";
        Map<String, String> headers = new HashMap<>();

        when(restTemplate.postForObject(
            eq(githubApiUrl + "/markdown"),
            any(Map.class),
            eq(String.class)
        )).thenReturn("<h1>Rendered Content</h1>");

        Renderer renderer = new GitHubRenderer(restTemplate, githubApiUrl, headers);
        String markdown = "# Test";
        Map<String, Object> context = new HashMap<>();

        String result = renderer.render(markdown, context);
        assertEquals("<h1>Rendered Content</h1>", result);
    }
}
