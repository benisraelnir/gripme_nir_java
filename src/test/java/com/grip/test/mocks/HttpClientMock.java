package com.grip.test.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grip.config.GripConstants;
import com.grip.test.TestHelpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

public class HttpClientMock extends HttpClient {
    private static final String[] auth = {"test-username", "test-password"};
    private final Map<String, Map<String, String>> responseMap;
    private final ObjectMapper objectMapper;
    private final boolean assertAllRequestsAreFired;
    private final Map<String, Integer> requestCounts;

    public HttpClientMock(boolean assertAllRequestsAreFired) {
        this.assertAllRequestsAreFired = assertAllRequestsAreFired;
        this.objectMapper = new ObjectMapper();
        this.responseMap = initializeResponseMap();
        this.requestCounts = new HashMap<>();
    }

    private Map<String, Map<String, String>> initializeResponseMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        try {
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

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
        String url = request.uri().toString();
        requestCounts.merge(url, 1, Integer::sum);

        // Validate request
        if (!url.endsWith("/markdown") && !url.endsWith("/markdown/raw")) {
            throw new IOException("Unexpected URL: " + url);
        }

        if (request.method() != "POST") {
            throw new IOException("Expected POST request, got: " + request.method());
        }

        // Get request body
        String requestBody = extractRequestBody(request);

        // Get response based on request
        String responseBody;
        if (url.endsWith("/markdown/raw")) {
            responseBody = getOutputForMarkdown(requestBody, null, null);
        } else {
            Map<String, Object> payload = objectMapper.readValue(requestBody, Map.class);
            responseBody = getOutputForMarkdown(
                    (String) payload.get("text"),
                    (String) payload.get("mode"),
                    (String) payload.get("context"));
        }

        System.out.println("Mock received request to " + url);
        System.out.println("Request body: " + requestBody);
        System.out.println("Response: " + responseBody);

        // Create response
        @SuppressWarnings("unchecked")
        HttpResponse<T> response = (HttpResponse<T>) new HttpResponseMock<>(
                request,
                200,
                HttpHeaders.of(Map.of("content-type", List.of("text/html")), (s1, s2) -> true),
                responseBody);

        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        try {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return sendAsync(request, responseBodyHandler);
    }

    private String extractRequestBody(HttpRequest request) throws IOException {
        HttpRequest.BodyPublisher publisher = request.bodyPublisher().get();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        
        class BytesSubscriber implements Flow.Subscriber<ByteBuffer> {
            private Flow.Subscription subscription;
            private final ByteArrayOutputStream bytes;
            
            BytesSubscriber(ByteArrayOutputStream bytes) {
                this.bytes = bytes;
            }
            
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }
            
            @Override
            public void onNext(ByteBuffer item) {
                bytes.writeBytes(item.array());
            }
            
            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
            
            @Override
            public void onComplete() {}
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        publisher.subscribe(new BytesSubscriber(bytes) {
            @Override
            public void onComplete() {
                future.complete(null);
            }
            
            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("Failed to read request body", e);
        }
        
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private String getOutputForMarkdown(String content, String mode, String context) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        Map<String, String> responses = responseMap.get(content);
        if (responses == null) {
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
        if (assertAllRequestsAreFired && requestCounts.isEmpty()) {
            throw new AssertionError("No requests were made to the mock");
        }
    }

    // Required overrides for abstract HttpClient methods
    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return Version.HTTP_2;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    // Mock HttpResponse implementation
    private static class HttpResponseMock<T> implements HttpResponse<T> {
        private final HttpRequest request;
        private final int statusCode;
        private final HttpHeaders headers;
        private final T body;

        public HttpResponseMock(HttpRequest request, int statusCode, HttpHeaders headers, T body) {
            this.request = request;
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return headers;
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public Version version() {
            return Version.HTTP_2;
        }
    }
}