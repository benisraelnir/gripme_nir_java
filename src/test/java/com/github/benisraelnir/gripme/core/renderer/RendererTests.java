package com.github.benisraelnir.gripme.core.renderer;

import com.github.benisraelnir.gripme.service.GitHubService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        // Create mock GitHubService
        GitHubService githubService = mock(GitHubService.class);
        when(githubService.renderMarkdown(anyString(), anyBoolean(), anyString()))
            .thenReturn("<h1>Rendered Content</h1>");

        // Create renderer with mock service
        Renderer renderer = new GitHubRenderer(githubService);

        // Test data
        String markdown = "# Test";
        Map<String, Object> context = new HashMap<>();
        context.put("gfm", true);
        context.put("context", "test/repo");

        // Execute and verify
        String result = renderer.render(markdown, context);
        assertEquals("<h1>Rendered Content</h1>", result);

        // Verify service was called with correct parameters
        verify(githubService).renderMarkdown(markdown, true, "test/repo");
    }
}
