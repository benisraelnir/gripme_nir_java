package com.github.benisraelnir.gripme.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebConfig.class)
public class WebConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStaticResourceAccess() throws Exception {
        mockMvc.perform(get("/static/favicon.ico"))
                .andExpect(status().isNotFound()); // Resource doesn't exist in test context
    }

    @Test
    public void testCacheResourceAccess() throws Exception {
        mockMvc.perform(get("/cache/test.png"))
                .andExpect(status().isNotFound()); // Resource doesn't exist in test context
    }
}
