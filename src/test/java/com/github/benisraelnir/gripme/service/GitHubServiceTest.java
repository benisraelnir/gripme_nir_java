package com.github.benisraelnir.gripme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GitHubServiceTest {
    private RestTemplate restTemplate;
    private GitHubService githubService;
    private static final String API_URL = "https://api.github.com";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        githubService = new GitHubService(restTemplate, API_URL);
    }

    @Test
    void testRenderMarkdownWithoutAuth() {
        String markdown = "# Test";
        String expectedHtml = "<h1>Test</h1>";

        ResponseEntity<String> mockResponse = new ResponseEntity<>(expectedHtml, HttpStatus.OK);
        when(restTemplate.postForObject(
            eq(API_URL + "/markdown"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(expectedHtml);

        String result = githubService.renderMarkdown(markdown, true, "user/repo");
        assertEquals(expectedHtml, result);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
            eq(API_URL + "/markdown"),
            requestCaptor.capture(),
            eq(String.class)
        );

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertNull(headers.get("Authorization"));
    }

    @Test
    void testRenderMarkdownWithAuth() {
        githubService.setCredentials("user", "pass");
        String markdown = "# Test";
        String expectedHtml = "<h1>Test</h1>";

        when(restTemplate.postForObject(
            eq(API_URL + "/markdown"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(expectedHtml);

        String result = githubService.renderMarkdown(markdown, true, "user/repo");
        assertEquals(expectedHtml, result);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
            eq(API_URL + "/markdown"),
            requestCaptor.capture(),
            eq(String.class)
        );

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertNotNull(headers.get("Authorization"));
        assertTrue(headers.get("Authorization").get(0).startsWith("Basic "));
    }

    @Test
    void testGetAssetWithAuth() {
        githubService.setCredentials("user", "pass");
        byte[] expectedData = "test data".getBytes();
        String path = "test/path";

        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(expectedData, HttpStatus.OK);
        when(restTemplate.exchange(
            eq(API_URL + "/raw?path=" + path),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenReturn(mockResponse);

        byte[] result = githubService.getAsset(path);
        assertArrayEquals(expectedData, result);

        ArgumentCaptor<HttpEntity<Void>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq(API_URL + "/raw?path=" + path),
            eq(HttpMethod.GET),
            requestCaptor.capture(),
            eq(byte[].class)
        );

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertNotNull(headers.get("Authorization"));
        assertTrue(headers.get("Authorization").get(0).startsWith("Basic "));
    }
}
