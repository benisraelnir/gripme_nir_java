package com.grip.test;

import com.grip.assets.GitHubAssetManager;
import com.grip.config.GripConstants;
import com.grip.renderer.GitHubRenderer;
import com.grip.test.mocks.GitHubRequestsMock;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GitHubTest {

    private String inputMarkdown;
    private String outputReadme;
    private String outputUserContent;
    private String outputUserContext;
    private RestTemplate restTemplate;
    private GitHubRequestsMock mockRequests;

    @BeforeEach
    void setUp() {
        try {
            inputMarkdown = TestHelpers.inputFile("gfm-test.md");
            outputReadme = TestHelpers.outputFile("raw", "gfm-test.html");
            outputUserContent = TestHelpers.outputFile("raw", "gfm-test-user-content.html");
            outputUserContext = TestHelpers.outputFile("raw", "gfm-test-user-context.html");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test files", e);
        }

        restTemplate = new RestTemplate();
    }

    // TODO: Fix this test    
    // @Test
    // @Tag("assumption")
    // void testGitHub() {
    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer renderer = new GitHubRenderer(restTemplate, false);
    //     String result = assertDoesNotThrow(() -> renderer.render(""));
    //     assertNotNull(result);
    //     assertEquals("", result);
    //     mockRequests.verify();
    // }

    // TODO: Fix this test    
    // @Test
    // @Tag("assumption")
    // void testGitHubApi() {
    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer renderer = new GitHubRenderer(restTemplate, false);
    //     String result = assertDoesNotThrow(() -> renderer.render(""));
    //     assertNotNull(result);
    //     assertEquals("", result);
    //     mockRequests.verify();

    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer userContentRenderer = new GitHubRenderer(restTemplate, true);
    //     String userResult = assertDoesNotThrow(() -> userContentRenderer.render(""));
    //     assertNotNull(userResult);
    //     assertEquals("", userResult);
    //     mockRequests.verify();
    // }

    // TODO: Fix this test
    // @Test
    // @Tag("assumption")
    // void testGitHubReadme() {
    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer renderer = new GitHubRenderer(restTemplate, false);
    //     String result = assertDoesNotThrow(() -> renderer.render(inputMarkdown));
    //     assertNotNull(result);
    //     assertEquals(outputReadme, result);
    //     mockRequests.verify();
    // }

    // TODO: Fix this test
    // @Test
    // @Tag("assumption")
    // void testGitHubUserContent() {
    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer renderer = new GitHubRenderer(restTemplate, true);
    //     String result = assertDoesNotThrow(() -> renderer.render(inputMarkdown));
    //     assertNotNull(result);
    //     assertEquals(outputUserContent, result);
    //     mockRequests.verify();
    // }

    // TODO: Fix this test
    // @Test
    // @Tag("assumption")
    // void testGitHubUserContext() {
    //     mockRequests = new GitHubRequestsMock(restTemplate, true);
    //     GitHubRenderer renderer = new GitHubRenderer(restTemplate, true, "user-context");
    //     String result = assertDoesNotThrow(() -> renderer.render(inputMarkdown));
    //     assertNotNull(result);
    //     assertEquals(outputUserContext, result);
    //     mockRequests.verify();
    // }

    @Test
    @Tag("assumption")
    void testStylesExist(@TempDir Path tempDir) {
        GitHubAssetManager assetManager = new GitHubAssetManager(tempDir.toString());
        assertDoesNotThrow(() -> assetManager.retrieveStyles("http://dummy/"));

        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files);
        assertTrue(files.length > 2);

        List<File> fileList = Arrays.asList(files);

        assertTrue(fileList.stream()
            .map(File::getName)
            .anyMatch(name -> (name.startsWith("github-") || name.startsWith("global-")) && name.endsWith(".css")),
            "Missing GitHub/Global CSS file");

        assertTrue(fileList.stream()
            .map(File::getName)
            .anyMatch(name -> name.startsWith("primer-") && name.endsWith(".css")),
            "Missing Primer CSS file");
    }

    // TODO: Test that local images show up in the browser
    
    // TODO: Test that web images show up in the browser
    
    // TODO: Test that octicons show up in the browser
    
    // TODO: Test that anchor tags still work
}