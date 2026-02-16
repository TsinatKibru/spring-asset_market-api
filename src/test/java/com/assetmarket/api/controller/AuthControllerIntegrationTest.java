package com.assetmarket.api.controller;

import com.assetmarket.api.dto.LoginRequest;
import com.assetmarket.api.dto.OnboardingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldOnboardCompanyAndLoginAdmin() throws Exception {
        // Use shorter unique ID to satisfy @Size(max = 20) constraints
        String uniqueId = "t" + (System.currentTimeMillis() % 1000000);

        // 1. Onboard
        OnboardingRequest onboardingRequest = OnboardingRequest.builder()
                .companyName("Company " + uniqueId)
                .slug("slug" + uniqueId)
                .adminUsername("admin" + uniqueId)
                .adminEmail("admin" + uniqueId + "@example.com")
                .adminPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/onboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(onboardingRequest)))
                .andExpect(status().isOk());

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin" + uniqueId);
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .header("X-Tenant-ID", onboardingRequest.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
