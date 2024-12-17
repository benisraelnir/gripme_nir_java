package com.github.benisraelnir.gripme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class GitHubService {
    private final RestTemplate restTemplate;
    private static final String MARKDOWN_ENDPOINT = "/markdown";
    private static final String RAW_ENDPOINT = "/raw";

    @Autowired
    public GitHubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String renderMarkdown(String content, boolean isGfm, String context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

        Map<String, Object> request = Map.of(
            "text", content,
            "mode", isGfm ? "gfm" : "markdown",
            "context", context
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(MARKDOWN_ENDPOINT, entity, String.class);
    }

    public byte[] getAsset(String path) {
        return restTemplate.getForObject(RAW_ENDPOINT + "?path=" + path, byte[].class);
    }
}
