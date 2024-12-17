package com.github.benisraelnir.gripme.controller;

import com.github.benisraelnir.gripme.service.GitHubService;
import com.github.benisraelnir.gripme.core.reader.Reader;
import com.github.benisraelnir.gripme.core.renderer.Renderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarkdownController.class)
public class MarkdownControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService githubService;

    @MockBean
    private Reader reader;

    @MockBean
    private Renderer renderer;

    @Test
    public void testRenderMarkdownWithContent() throws Exception {
        String markdown = "# Test";
        String rendered = "<h1>Test</h1>";
        when(renderer.render(markdown)).thenReturn(rendered);

        mockMvc.perform(post("/api/render")
                .contentType(MediaType.TEXT_PLAIN)
                .content(markdown))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(rendered));
    }

    @Test
    public void testRenderMarkdownWithPath() throws Exception {
        String path = "test.md";
        String markdown = "# Test";
        String rendered = "<h1>Test</h1>";
        when(reader.read(path)).thenReturn(markdown);
        when(renderer.render(markdown)).thenReturn(rendered);

        mockMvc.perform(post("/api/render")
                .param("path", path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(rendered));
    }

    @Test
    public void testRenderMarkdownRaw() throws Exception {
        String markdown = "# Test";
        String rendered = "<h1>Test</h1>";
        when(renderer.renderRaw(markdown)).thenReturn(rendered);

        mockMvc.perform(post("/api/render")
                .param("raw", "true")
                .contentType(MediaType.TEXT_PLAIN)
                .content(markdown))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(rendered));
    }

    @Test
    public void testGetAsset() throws Exception {
        String path = "test.png";
        byte[] asset = "test".getBytes();
        when(githubService.getAsset(path)).thenReturn(asset);

        mockMvc.perform(get("/api/asset")
                .param("path", path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(asset));
    }
}
