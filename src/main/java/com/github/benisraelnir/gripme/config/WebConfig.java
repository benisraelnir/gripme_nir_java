package com.github.benisraelnir.gripme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${gripme.static.path:/static}")
    private String staticPath;

    @Value("${gripme.cache.path:/cache}")
    private String cachePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:" + staticPath + "/");

        registry.addResourceHandler("/cache/**")
                .addResourceLocations("file:" + cachePath + "/");
    }
}
