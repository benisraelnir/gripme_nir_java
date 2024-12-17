package com.github.benisraelnir.gripme.controller;

import com.github.benisraelnir.gripme.service.GitHubService;
import com.github.benisraelnir.gripme.core.reader.Reader;
import com.github.benisraelnir.gripme.core.renderer.Renderer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarkdownController {
    private final GitHubService githubService;
    private final Reader reader;
    private final Renderer renderer;

    @PostMapping(value = "/render", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renderMarkdown(@RequestBody(required = false) String content,
                                               @RequestParam(required = false) String path,
                                               @RequestParam(defaultValue = "false") boolean raw) throws Exception {
        String markdownContent = content;
        if (path != null) {
            markdownContent = reader.read(path);
        }

        String renderedHtml = raw ?
            renderer.renderRaw(markdownContent) :
            renderer.render(markdownContent, Collections.emptyMap());

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(renderedHtml);
    }

    @GetMapping(value = "/asset/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getAsset(@RequestParam String path) {
        byte[] asset = githubService.getAsset(path);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(asset);
    }
}
