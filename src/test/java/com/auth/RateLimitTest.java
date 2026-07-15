package com.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.auth.config.ratelimits.RateLimitingComponent;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(printOnlyOnFailure=true)
public class RateLimitTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private RateLimitingComponent rateLimitingComponent;

    @BeforeEach
    void setUp() {
        rateLimitingComponent.clearCache();
    }

    @Test
    void registerLimit() throws Exception {
        rateLimitingComponent.clearCache();
    	
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/public/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user" + i + "\",\"password\":\"pass\"}"));
        }

        mockMvc.perform(post("/api/public/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"overflow\",\"password\":\"pass\"}"))
            .andExpect(status().isTooManyRequests());
    }
    
//    @Test
//    void loginLimit() throws Exception {
//        rateLimitingComponent.clearCache();
//    	
//        for (int i = 0; i < 10; i++) {
//            mockMvc.perform(post("/api/public/login")
//                .with(csrf())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"username\":\"user" + i + "\",\"password\":\"pass\"}"));
//        }
//
//        mockMvc.perform(post("/api/public/login")
//            .with(csrf())
//            .contentType(MediaType.APPLICATION_JSON)
//            .content("{\"username\":\"overflow\",\"password\":\"pass\"}"))
//            .andExpect(status().isTooManyRequests());
//    }
}