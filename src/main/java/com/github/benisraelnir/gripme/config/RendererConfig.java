package com.github.benisraelnir.gripme.config;

import com.github.benisraelnir.gripme.core.renderer.GitHubRenderer;
import com.github.benisraelnir.gripme.core.renderer.Renderer;
import com.github.benisraelnir.gripme.service.GitHubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RendererConfig {
    @Bean
    @Primary
    public Renderer defaultRenderer(GitHubService githubService) {
        return new GitHubRenderer(githubService);
    }
}
