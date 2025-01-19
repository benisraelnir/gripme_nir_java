package com.grip.test;

import com.grip.core.Grip;
import com.grip.exceptions.ReadmeNotFoundException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@TestConfiguration
@ComponentScan(
    basePackages = "com.grip",
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.grip\\.config\\..*")
)
public class ApiTestConfig {
    @Bean
    @Primary
    public Grip grip() throws ReadmeNotFoundException {
        // Use test resources input directory as root
        return new Grip(TestHelpers.inputFilename());
    }
}