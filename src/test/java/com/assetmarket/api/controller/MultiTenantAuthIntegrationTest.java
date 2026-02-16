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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MultiTenantAuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldAllowSameUsernameInDifferentTenants() throws Exception {
                String commonUsername = "alex";
                String commonPassword = "password123";

                // 1. Onboard Tenant A with user 'alex'
                OnboardingRequest onboardA = OnboardingRequest.builder()
                                .companyName("Tenant A")
                                .slug("tenant-a")
                                .adminUsername(commonUsername)
                                .adminEmail("alex@a.com")
                                .adminPassword(commonPassword)
                                .build();

                mockMvc.perform(post("/api/v1/onboard")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onboardA)))
                                .andExpect(status().isOk());

                // 2. Onboard Tenant B with SAME user 'alex'
                OnboardingRequest onboardB = OnboardingRequest.builder()
                                .companyName("Tenant B")
                                .slug("tenant-b")
                                .adminUsername(commonUsername)
                                .adminEmail("alex@b.com")
                                .adminPassword(commonPassword)
                                .build();

                mockMvc.perform(post("/api/v1/onboard")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onboardB)))
                                .andExpect(status().isOk());

                // 3. Login as 'alex' for Tenant A
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(commonUsername);
                loginRequest.setPassword(commonPassword);

                mockMvc.perform(post("/api/v1/auth/login")
                                .header("X-Tenant-ID", "tenant-a")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.tenantId").value("tenant-a"));

                // 4. Login as 'alex' for Tenant B
                mockMvc.perform(post("/api/v1/auth/login")
                                .header("X-Tenant-ID", "tenant-b")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.tenantId").value("tenant-b"));

                // 5. Login fails with wrong tenant ID
                mockMvc.perform(post("/api/v1/auth/login")
                                .header("X-Tenant-ID", "tenant-c")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailLoginWithoutTenantHeader() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("alex");
                loginRequest.setPassword("password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isBadRequest());
        }
}
