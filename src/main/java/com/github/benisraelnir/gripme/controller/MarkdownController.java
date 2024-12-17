package com.github.benisraelnir.gripme.controller;

import com.github.benisraelnir.gripme.service.GitHubService;
import com.github.benisraelnir.gripme.core.reader.Reader;
import com.github.benisraelnir.gripme.core.renderer.Renderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarkdownController {
    private final GitHubService githubService;
    private final Reader reader;
    private final Renderer renderer;

    @Autowired
    public MarkdownController(GitHubService githubService, Reader reader, Renderer renderer) {
        this.githubService = githubService;
        this.reader = reader;
        this.renderer = renderer;
    }

    @PostMapping(value = "/render", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public String renderMarkdown(@RequestBody(required = false) String content,
                               @RequestParam(required = false) String path,
                               @RequestParam(defaultValue = "false") boolean raw) throws Exception {
        String markdown = content;
        if (path != null) {
            markdown = reader.read(path);
        }

        if (raw) {
            return renderer.renderRaw(markdown);
        }
        return renderer.render(markdown, Collections.emptyMap());
    }

    @GetMapping(value = "/asset", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] getAsset(@RequestParam String path) throws Exception {
        return githubService.getAsset(path);
    }
}
