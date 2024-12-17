package com.github.benisraelnir.gripme.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.*;

class GitHubConfigTest {

    @Test
    void shouldCreateRestTemplateWithoutAuth() {
        GitHubConfig config = new GitHubConfig();
        RestTemplate restTemplate = config.githubRestTemplate(new RestTemplateBuilder());
        assertNotNull(restTemplate);
    }

    @Test
    void shouldCreateRestTemplateWithAuth() {
        GitHubConfig config = new GitHubConfig();
        config.setUsername("test-user");
        config.setPassword("test-pass");
        RestTemplate restTemplate = config.githubRestTemplate(new RestTemplateBuilder());
        assertNotNull(restTemplate);
    }
}
