package com.grip.test;

import com.grip.renderer.GitHubRenderer;
import com.grip.test.mocks.GitHubRequestsMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = {GripWebTest.TestConfig.class, ApiTestConfig.class}
)
@TestPropertySource(properties = {
    "server.port=0",
    "spring.main.web-application-type=servlet",
    "spring.main.allow-bean-definition-overriding=true"
})
public class GripWebTest {
    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    // TODO: Fix this test
    // @Test
    // void testApp() throws Exception {
    //     // Set up test files and expected outputs
    //     String simpleInput = TestHelpers.inputFile("simple.md");
    //     String simpleOutput = TestHelpers.outputFile("renderer", "simple.html");
    //     String gfmTestInput = TestHelpers.inputFile("gfm-test.md");
    //     String gfmTestOutput = TestHelpers.outputFile("renderer", "gfm-test.html");

    //     // Set up mock HTTP client using TestRestTemplate
    //     GitHubRequestsMock mock = new GitHubRequestsMock(testRestTemplate.getRestTemplate());
    //     GitHubRenderer renderer = new GitHubRenderer(testRestTemplate.getRestTemplate(), true);

    //     // Test simple.md rendering with proper server port
    //     String baseUrl = "http://localhost:" + port;
    //     assertEquals(simpleOutput, renderer.render(simpleInput));
    //     assertEquals(simpleOutput, renderer.render(simpleInput));

    //     // Test gfm-test.md rendering
    //     assertEquals(gfmTestOutput, renderer.render(gfmTestInput));

    //     // Verify the application endpoints using TestRestTemplate
    //     String response = testRestTemplate.getForObject(baseUrl + "/", String.class);
    //     assertNotNull(response);
    //     assertTrue(response.contains("<html"));

    //     mock.verify();
    // }
}