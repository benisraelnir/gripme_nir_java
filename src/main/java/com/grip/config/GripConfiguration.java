package com.grip.config;

import com.grip.reader.ReadmeReader;
import com.grip.reader.DirectoryReader;
import com.grip.renderer.ReadmeRenderer;
import com.grip.renderer.GitHubRenderer;
import com.grip.assets.ReadmeAssetManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for Grip service.
 * Provides default beans for core components.
 */
@Configuration
public class GripConfiguration {

    /**
     * Creates a default ReadmeReader bean if none is provided.
     *
     * @return Default DirectoryReader instance
     */
    @Bean
    @ConditionalOnMissingBean
    public ReadmeReader defaultReader() {
        try {
            return new DirectoryReader(null);
        } catch (Exception e) {
            // Fallback to safe reader that always returns empty content
            return new ReadmeReader() {
                @Override
                public Object read(String subpath) {
                    return "No content available";
                }
            };
        }
    }

    /**
     * Creates a default ReadmeRenderer bean if none is provided.
     *
     * @return Default GitHubRenderer instance
     */
    @Bean
    @ConditionalOnMissingBean
    public ReadmeRenderer defaultRenderer() {
        return new GitHubRenderer();
    }
}