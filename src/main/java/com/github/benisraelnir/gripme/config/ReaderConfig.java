package com.github.benisraelnir.gripme.config;

import com.github.benisraelnir.gripme.core.reader.DirectoryReader;
import com.github.benisraelnir.gripme.core.reader.Reader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;

@Configuration
public class ReaderConfig {
    @Bean
    public Path defaultPath() {
        return Path.of(System.getProperty("user.dir"));
    }

    @Bean
    @Primary
    public Reader defaultReader(Path defaultPath) {
        return new DirectoryReader(defaultPath);
    }
}
