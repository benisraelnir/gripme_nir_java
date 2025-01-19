package com.grip.test.utils;

import com.grip.renderer.GitHubRenderer;
import com.grip.test.TestHelpers;
import com.grip.test.mocks.GripMock;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Regenerates all the rendered Markdown files in the output/ directory.
 */
public class RegenerateTestOutputs {
    private static final Object AUTH = null; // Using null auth for testing purposes
    private static final Path TEST_RESOURCES = Paths.get(TestHelpers.getTestResourcesPath());
    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * Writes text content to a file in the output directory.
     *
     * @param text  The content to write
     * @param parts Path parts to be joined to form the output file path
     * @throws IOException If an I/O error occurs
     */
    private static void write(String text, String... parts) throws IOException {
        Path outputPath = TEST_RESOURCES.resolve("output");
        for (String part : parts) {
            outputPath = outputPath.resolve(part);
        }
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Regenerates app-related test output files.
     *
     * @throws Exception If an error occurs during regeneration
     */
    private static void regenerateApp() throws Exception {
        String zero = TestHelpers.inputFilename("zero.md");
        String simple = TestHelpers.inputFilename("simple.md");
        String gfmTest = TestHelpers.inputFilename("gfm-test.md");

        // Zero markdown tests
        write(new GripMock(zero, AUTH, new GitHubRenderer(restTemplate)).render(),
                "app", "zero.html");
        write(new GripMock(zero, AUTH, new GitHubRenderer(restTemplate, true)).render(),
                "app", "zero-user-context.html");
        write(new GripMock(zero, AUTH, new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT)).render(),
                "app", "zero-user-context.html");

        // Simple markdown tests
        write(new GripMock(simple, AUTH, new GitHubRenderer(restTemplate)).render(),
                "app", "simple.html");
        write(new GripMock(simple, AUTH, new GitHubRenderer(restTemplate, true)).render(),
                "app", "simple-user-context.html");
        write(new GripMock(simple, AUTH, new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT)).render(),
                "app", "simple-user-context.html");

        // GFM test markdown
        write(new GripMock(gfmTest, AUTH, new GitHubRenderer(restTemplate)).render(),
                "app", "gfm-test.html");
        write(new GripMock(gfmTest, AUTH, new GitHubRenderer(restTemplate, true)).render(),
                "app", "gfm-test-user-context.html");
        write(new GripMock(gfmTest, AUTH, new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT)).render(),
                "app", "gfm-test-user-context.html");
    }

    /**
     * Regenerates exporter-related test output files.
     * TODO: Implement [comment from source project]
     * TODO: Strip out inlined CSS specifics? [comment from source project]
     */
    private static void regenerateExporter() {
        // TODO: Implement [comment from source project]
        // TODO: Strip out inlined CSS specifics? [comment from source project]
    }

    /**
     * Regenerates renderer-related test output files.
     *
     * @throws Exception If an error occurs during regeneration
     */
    private static void regenerateRenderer() throws Exception {
        String simple = TestHelpers.inputFile("simple.md");
        String gfmTest = TestHelpers.inputFile("gfm-test.md");

        write(new GitHubRenderer(restTemplate).render(simple, AUTH),
                "renderer", "simple.html");
        write(new GitHubRenderer(restTemplate, true).render(simple, AUTH),
                "renderer", "simple-user-content.html");
        write(new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT).render(simple, AUTH),
                "renderer", "simple-user-context.html");

        write(new GitHubRenderer(restTemplate).render(gfmTest, AUTH),
                "renderer", "gfm-test.html");
        write(new GitHubRenderer(restTemplate, true).render(gfmTest, AUTH),
                "renderer", "gfm-test-user-content.html");
        write(new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT).render(gfmTest, AUTH),
                "renderer", "gfm-test-user-context.html");
    }

    /**
     * Regenerates raw output test files.
     *
     * @throws Exception If an error occurs during regeneration
     */
    private static void regenerateRaw() throws Exception {
        String zero = TestHelpers.inputFile("zero.md");
        String simple = TestHelpers.inputFile("simple.md");
        String gfmTest = TestHelpers.inputFile("gfm-test.md");

        write(new GitHubRenderer(restTemplate, false, null, null, true).render(zero, AUTH),
                "raw", "zero.html");
        write(new GitHubRenderer(restTemplate, true, null, null, true).render(zero, AUTH),
                "raw", "zero-user-content.html");
        write(new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT, null, true).render(zero, AUTH),
                "raw", "zero-user-context.html");

        write(new GitHubRenderer(restTemplate, false, null, null, true).render(simple, AUTH),
                "raw", "simple.html");
        write(new GitHubRenderer(restTemplate, true, null, null, true).render(simple, AUTH),
                "raw", "simple-user-content.html");
        write(new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT, null, true).render(simple, AUTH),
                "raw", "simple-user-context.html");

        write(new GitHubRenderer(restTemplate, false, null, null, true).render(gfmTest, AUTH),
                "raw", "gfm-test.html");
        write(new GitHubRenderer(restTemplate, true, null, null, true).render(gfmTest, AUTH),
                "raw", "gfm-test-user-content.html");
        write(new GitHubRenderer(restTemplate, true, TestHelpers.USER_CONTEXT, null, true)
                .render(gfmTest, AUTH), "raw", "gfm-test-user-context.html");
    }

    /**
     * Regenerates all test output files.
     *
     * @throws Exception If an error occurs during regeneration
     */
    public static void regenerate() throws Exception {
        System.out.println("Regenerating output files...");
        regenerateApp();
        regenerateExporter();
        regenerateRenderer();
        regenerateRaw();
    }

    public static void main(String[] args) throws Exception {
        regenerate();
    }
}