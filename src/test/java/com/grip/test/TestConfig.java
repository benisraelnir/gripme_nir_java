package com.grip.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.grip.command.GripCommandLine;
import com.grip.reader.DirectoryReader;
import com.grip.core.Grip;
import com.grip.exceptions.ReadmeNotFoundException;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import jakarta.annotation.PostConstruct;

/**
 * Test configuration class that ensures test resources are properly configured.
 * This is the Java equivalent of the Python conftest.py file.
 * While Python needed explicit path configuration, in Spring Boot this is handled
 * more elegantly through the framework's resource handling.
 */
@TestConfiguration
public class TestConfig {
    
    @MockBean
    private GripCommandLine gripCommandLine;
    
    @MockBean
    private DirectoryReader directoryReader;
    
    @Bean
    public Grip grip() throws ReadmeNotFoundException {
        return new Grip(directoryReader);
    }
    
    @PostConstruct
    public void configureDirectoryReader() throws Exception {
        // Configure mock DirectoryReader
        when(directoryReader.read(any())).thenReturn("# Test Content\n\nThis is test content.");
        when(directoryReader.normalizeSubpath(any())).thenReturn("");
        when(directoryReader.isBinary(any())).thenReturn(false);
        when(directoryReader.filenameFor(any())).thenReturn("README.md");
    }
    
    /**
     * Configures test resources location.
     * In the Python version, this was done by modifying sys.path.
     * In Spring Boot, we use resource handling instead.
     */
    @Bean
    public Resource testResourcesLocation() {
        return new ClassPathResource(""); // Points to src/test/resources
    }
}