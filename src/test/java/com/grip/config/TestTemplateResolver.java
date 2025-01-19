package com.grip.config;

import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.ICacheEntryValidity;
import java.time.Duration;
import java.time.Instant;

import java.util.Map;
import java.util.HashMap;

public class TestTemplateResolver implements ITemplateResolver {
    private final Map<String, String> templates = new HashMap<>();
    private final TemplateMode templateMode;
    private final String characterEncoding;

    public TestTemplateResolver() {
        this.templateMode = TemplateMode.HTML;
        this.characterEncoding = "UTF-8";
    }

    public void addTemplate(String name, String content) {
        templates.put(name, content);
    }

    @Override
    public String getName() {
        return "TEST_TEMPLATE_RESOLVER";
    }

    @Override
    public Integer getOrder() {
        return 0;
    }

    @Override
    public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate, 
            String template, Map<String, Object> templateResolutionAttributes) {
        String templateContent = templates.get(template);
        if (templateContent == null) {
            return null;
        }

        ITemplateResource templateResource = new StringTemplateResource(templateContent);
        return new TemplateResolution(
            templateResource,
            templateMode,
            new ICacheEntryValidity() {
                private final Instant creationTime = Instant.now();
                
                @Override
                public boolean isCacheable() {
                    return true;
                }

                @Override
                public boolean isCacheStillValid() {
                    return true;
                }
            }
        );
    }

    
}