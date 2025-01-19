package com.grip.renderer;

import com.grip.config.GripConstants;
import com.grip.util.HtmlPatcher;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Renders the specified Readme using the GitHub Markdown API.
 */
public class GitHubRenderer extends ReadmeRenderer {
    private final String apiUrl;
    private final boolean raw;
    private final HttpClient httpClient;
    private final RestTemplate restTemplate;

    /**
     * Constructs a GitHubRenderer with specified settings.
     *
     * @param restTemplate the RestTemplate to use for HTTP requests
     * @param userContent whether the content is user-generated
     * @param context the context for rendering
     * @param apiUrl the GitHub API URL
     * @param raw whether to return raw HTML
     */
    public GitHubRenderer(RestTemplate restTemplate, boolean userContent, String context, String apiUrl, boolean raw) {
        super(userContent, context);
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl != null ? apiUrl : GripConstants.DEFAULT_API_URL;
        this.raw = raw;
        this.httpClient = HttpClient.newHttpClient();
    }
    public GitHubRenderer(Boolean userContent, String context, String apiUrl, Boolean raw) {
        this(
            null, // RestTemplate is null
            userContent != null && userContent, // Default to false if userContent is null
            context,
            apiUrl,
            raw != null && raw // Default to false if raw is null
        );
    }

    /**
     * Constructs a GitHubRenderer with default settings.
     */
    public GitHubRenderer() {
        this(null, false, null, null, false);
    }

    public GitHubRenderer(RestTemplate restTemplate, boolean userContent, String context) {
        this(restTemplate, userContent, context, GripConstants.DEFAULT_API_URL, false);
    }
        
    /**
     * Constructs a GitHubRenderer with RestTemplate and user content setting.
     *
     * @param restTemplate the RestTemplate to use for HTTP requests
     * @param userContent whether the content is user-generated
     */
    public GitHubRenderer(RestTemplate restTemplate, boolean userContent) {
        this(restTemplate, userContent, null, GripConstants.DEFAULT_API_URL, false);
    }

    /**
     * Constructs a GitHubRenderer with RestTemplate and default user content.
     *
     * @param restTemplate the RestTemplate to use for HTTP requests
     */
    public GitHubRenderer(RestTemplate restTemplate) {
        this(restTemplate, false);
    }

    /**
     * Renders the specified markdown content and embedded styles.
     *
     * @param text the markdown text to render
     * @param auth authentication credentials (optional)
     * @return the rendered content
     * @throws Exception if the request fails or text is not a string
     */
    @Override
    public String render(String text, Object auth) throws Exception {
        if (!(text instanceof String)) {
            throw new IllegalArgumentException("Expected a string, got " + (text == null ? "null" : text.getClass().getName()));
        }

        String url = apiUrl + "/markdown";
        HttpHeaders headers = new HttpHeaders();
        byte[] requestBody;

        HttpRequest.Builder requestBuilder;

        if (raw) {
            url = apiUrl + "/markdown/raw";
            requestBody = text.getBytes(StandardCharsets.UTF_8);
            requestBuilder = HttpRequest.newBuilder()
                    .header("content-type", "text/x-markdown; charset=UTF-8");
        } else {
            url = apiUrl + "/markdown";
            JSONObject data = new JSONObject();
            data.put("text", text);
            data.put("mode", "gfm");
            if (context != null) {
                data.put("context", context);
            }
            requestBody = data.toString().getBytes(StandardCharsets.UTF_8);
            requestBuilder = HttpRequest.newBuilder()
                    .header("content-type", "application/json; charset=UTF-8");
        }

        if (restTemplate != null) {
            headers.setContentType(userContent ? MediaType.APPLICATION_JSON : MediaType.parseMediaType("text/x-markdown; charset=UTF-8"));
            HttpEntity<byte[]> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("GitHub API request failed with status: " + response.getStatusCode());
            }

            return raw ? response.getBody() : HtmlPatcher.patch(response.getBody());
        } else {
            HttpRequest request = requestBuilder
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                throw new RuntimeException("GitHub API request failed with status: " + response.statusCode());
            }

            return raw ? response.body() : HtmlPatcher.patch(response.body());
        }
    }
}
