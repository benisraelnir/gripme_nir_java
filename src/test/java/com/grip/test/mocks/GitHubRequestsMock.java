package com.grip.test.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grip.config.GripConstants;
import com.grip.test.TestHelpers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Mock implementation for GitHub API requests used in unit testing.
 * This class simulates GitHub API responses for markdown rendering.
 */
public class GitHubRequestsMock {
    private static final String[] auth = {"test-username", "test-password"};
    private static final String[] badAuth = {"bad-username", "bad-password"};
    private final MockRestServiceServer mockServer;
    private final boolean assertAllRequestsAreFired;
    private final Map<String, Map<String, String>> responseMap;
    private final ObjectMapper objectMapper;
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    public GitHubRequestsMock(RestTemplate restTemplate) {
        this(restTemplate, false);
    }

    public GitHubRequestsMock(RestTemplate restTemplate, boolean assertAllRequestsAreFired) {
        this.mockServer = MockRestServiceServer.createServer(restTemplate);
        this.assertAllRequestsAreFired = assertAllRequestsAreFired;
        this.objectMapper = new ObjectMapper();
        this.responseMap = initializeResponseMap();
        setupMockResponses();
    }

    private Map<String, Map<String, String>> initializeResponseMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        try {
            // Empty string responses
            Map<String, String> emptyResponses = new HashMap<>();
            emptyResponses.put("markdown", "");
            emptyResponses.put("user-content", "");
            emptyResponses.put("user-context", "");
            map.put("", emptyResponses);

            // Zero markdown file responses
            Map<String, String> zeroResponses = new HashMap<>();
            zeroResponses.put("markdown", TestHelpers.outputFile("raw", "zero.html"));
            zeroResponses.put("user-content", TestHelpers.outputFile("raw", "zero-user-content.html"));
            zeroResponses.put("user-context", TestHelpers.outputFile("raw", "zero-user-context.html"));
            map.put(TestHelpers.inputFile("zero.md"), zeroResponses);

            // Simple markdown file responses
            Map<String, String> simpleResponses = new HashMap<>();
            simpleResponses.put("markdown", TestHelpers.outputFile("raw", "simple.html"));
            simpleResponses.put("user-content", TestHelpers.outputFile("raw", "simple-user-content.html"));
            simpleResponses.put("user-context", TestHelpers.outputFile("raw", "simple-user-context.html"));
            map.put(TestHelpers.inputFile("simple.md"), simpleResponses);

            // GFM test markdown file responses
            Map<String, String> gfmResponses = new HashMap<>();
            gfmResponses.put("markdown", TestHelpers.outputFile("raw", "gfm-test.html"));
            gfmResponses.put("user-content", TestHelpers.outputFile("raw", "gfm-test-user-content.html"));
            gfmResponses.put("user-context", TestHelpers.outputFile("raw", "gfm-test-user-context.html"));
            map.put(TestHelpers.inputFile("gfm-test.md"), gfmResponses);

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize response map", e);
        }
        return map;
    }

    private void setupMockResponses() {
        // Setup markdown endpoint for a single request
        mockServer.expect(MockRestRequestMatchers.requestTo(GripConstants.DEFAULT_API_URL + "/markdown"))
                .andExpect(MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
                .andExpect(request -> {
                    Object[] authResponse = validateAuthentication(request.getHeaders());
                    if (authResponse != null) {
                        throw new IOException((String) authResponse[2]);
                    }
                })
                .andRespond(request -> {
                    String body = decodeBody((MockClientHttpRequest) request);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = objectMapper.readValue(body, MAP_TYPE_REF);
                    String response = getOutputForMarkdown(
                            (String) payload.get("text"),
                            (String) payload.get("mode"),
                            (String) payload.get("context"));
                    return MockRestResponseCreators.withSuccess()
                            .contentType(MediaType.TEXT_HTML)
                            .body(response)
                            .createResponse(request);
                });

        // Setup markdown/raw endpoint
        mockServer.expect(MockRestRequestMatchers.requestTo(GripConstants.DEFAULT_API_URL + "/markdown/raw"))
                .andExpect(MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
                .andExpect(request -> {
                    Object[] authResponse = validateAuthentication(request.getHeaders());
                    if (authResponse != null) {
                        throw new IOException((String) authResponse[2]);
                    }
                })
                .andRespond(request -> {
                    String content = decodeBody((MockClientHttpRequest) request);
                    String response = getOutputForMarkdown(content, null, null);
                    return MockRestResponseCreators.withSuccess()
                            .contentType(MediaType.TEXT_HTML)
                            .body(response)
                            .createResponse(request);
                });
    }

    private String decodeBody(MockClientHttpRequest request) {
        byte[] body = request.getBodyAsBytes();
        if (body == null || body.length == 0) {
            return "";
        }
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.contains("application/json")) {
            return new String(body, StandardCharsets.UTF_8);
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private Object[] validateAuthentication(HttpHeaders headers) {
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            return null;
        }

        String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString((auth[0] + ":" + auth[1]).getBytes(StandardCharsets.UTF_8));
        String actualAuth = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (!expectedAuth.equals(actualAuth)) {
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("content-type", "application/json; charset=utf-8");
            return new Object[]{401, responseHeaders, "{\"message\":\"Bad credentials\"}"};
        }
        return null;
    }

    private String getOutputForMarkdown(String content, String mode, String context) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        Map<String, String> responses = responseMap.get(content);
        if (responses == null) {
            if (content.trim().isEmpty()) {
                return "";
            }
            throw new IllegalArgumentException("Markdown group not found for: " + content);
        }

        if (mode == null || mode.equals("markdown")) {
            return responses.get("markdown");
        } else if (context == null) {
            return responses.get("user-content");
        } else if (TestHelpers.USER_CONTEXT.equals(context)) {
            return responses.get("user-context");
        } else {
            throw new IllegalArgumentException("Markdown group not found for user context: " + context);
        }
    }

    public void verify() {
        mockServer.verify();
    }
}