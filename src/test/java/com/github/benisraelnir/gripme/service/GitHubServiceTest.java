package com.github.benisraelnir.gripme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {
    @Mock
    private RestTemplate restTemplate;

    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        gitHubService = new GitHubService(restTemplate);
    }

    @Test
    void renderMarkdownShouldCallGitHubAPI() {
        String markdown = "# Test";
        String expectedHtml = "<h1>Test</h1>";

        when(restTemplate.postForObject(
            eq("/markdown"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(expectedHtml);

        String result = gitHubService.renderMarkdown(markdown, true, "user/repo");
        assertEquals(expectedHtml, result);
    }

    @Test
    void renderMarkdownShouldHandleRateLimitError() {
        String markdown = "# Test";
        when(restTemplate.postForObject(
            eq("/markdown"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "API rate limit exceeded"));

        assertThrows(HttpClientErrorException.class, () ->
            gitHubService.renderMarkdown(markdown, true, "user/repo"));
    }

    @Test
    void renderMarkdownShouldHandleAuthenticationError() {
        String markdown = "# Test";
        when(restTemplate.postForObject(
            eq("/markdown"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        assertThrows(HttpClientErrorException.class, () ->
            gitHubService.renderMarkdown(markdown, true, "user/repo"));
    }
}
