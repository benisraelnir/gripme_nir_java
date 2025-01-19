package com.grip;

import com.grip.core.Grip;
import com.grip.reader.DirectoryReader;
import com.grip.renderer.ReadmeRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.grip.test.TestConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.grip.config.TestTemplateResolver;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import com.grip.exceptions.ReadmeNotFoundException;
import java.io.IOException;

@SpringBootTest
@Import(TestConfig.class)
class GripApplicationTests {

    @MockBean
    private ReadmeRenderer renderer;
    
    @Autowired
    private SpringTemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws Exception {
        
        
        // Configure mock renderer
        when(renderer.render(any(), any())).thenReturn("<h1>Test Content</h1>");
        when(renderer.isUserContent()).thenReturn(true);
        
        // Configure Grip to use mocked renderer
        ReflectionTestUtils.setField(grip, "renderer", renderer);
        
        // Configure template engine with test resolver
        TestTemplateResolver testResolver = new TestTemplateResolver();
        testResolver.addTemplate("index", "<!DOCTYPE html><html><body><div th:utext=\"${content}\"></div></body></html>");
        testResolver.addTemplate("limit", "<!DOCTYPE html><html><body><div>Rate limit</div></body></html>");
        templateEngine.setTemplateResolver(testResolver);
        
        
    }

    @Autowired
    private Grip grip;

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

    @Test
    void testTemplateRendering() throws Exception {
        // Prepare model
        Model model = new ExtendedModelMap();
        
        // Test rendering
        ResponseEntity<?> response = grip.renderRoot(model);
        
        // Verify response
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof String);
        String content = (String) response.getBody();
        System.out.println("Rendered content: " + content);
        assertTrue(content.contains("<h1>Test Content</h1>"), "Content should contain rendered markdown");
    }
}
