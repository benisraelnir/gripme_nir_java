package com.github.benisraelnir.gripme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Service
public class GitHubService {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private String username;
    private String password;

    @Autowired
    public GitHubService(RestTemplate restTemplate, @Value("${github.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String renderMarkdown(String content, boolean isGfm, String context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

        if (username != null && password != null) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        Map<String, Object> request = Map.of(
            "text", content,
            "mode", isGfm ? "gfm" : "markdown",
            "context", context
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(apiUrl + "/markdown", entity, String.class);
    }

    public byte[] getAsset(String path) {
        HttpHeaders headers = new HttpHeaders();
        if (username != null && password != null) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            apiUrl + "/raw?path=" + path,
            org.springframework.http.HttpMethod.GET,
            entity,
            byte[].class
        ).getBody();
    }
}
