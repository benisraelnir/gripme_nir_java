package com.github.benisraelnir.gripme.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@ConfigurationProperties(prefix = "github")
public class GitHubConfig {
    private String apiUrl = "https://api.github.com";
    private String username;
    private String password;
    private Duration timeout = Duration.ofSeconds(10);

    @Bean
    public RestTemplate githubRestTemplate(RestTemplateBuilder builder) {
        RestTemplateBuilder templateBuilder = builder
            .rootUri(apiUrl)
            .setConnectTimeout(timeout)
            .setReadTimeout(timeout)
            .interceptors(Collections.singletonList(new RateLimitInterceptor()));

        // Add basic auth if credentials are provided
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            templateBuilder = templateBuilder.basicAuthentication(username, password);
        }

        return templateBuilder.build();
    }

    private static class RateLimitInterceptor implements ClientHttpRequestInterceptor {
        private static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
        private static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";
        private static final AtomicInteger remainingRequests = new AtomicInteger(60);
        private static final AtomicLong resetTime = new AtomicLong(0);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution)
                throws IOException {
            // Check if we need to wait for rate limit reset
            long currentResetTime = resetTime.get();
            if (currentResetTime > 0 && remainingRequests.get() <= 0) {
                long now = Instant.now().getEpochSecond();
                if (now < currentResetTime) {
                    try {
                        Thread.sleep((currentResetTime - now) * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Rate limit wait interrupted", e);
                    }
                }
            }

            ClientHttpResponse response = execution.execute(request, body);

            // Update rate limit information from response headers
            HttpHeaders headers = response.getHeaders();
            String remaining = headers.getFirst(RATE_LIMIT_REMAINING);
            String reset = headers.getFirst(RATE_LIMIT_RESET);

            if (remaining != null) {
                remainingRequests.set(Integer.parseInt(remaining));
            }
            if (reset != null) {
                resetTime.set(Long.parseLong(reset));
            }

            return response;
        }
    }

    // Getters and setters for configuration properties
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
}
