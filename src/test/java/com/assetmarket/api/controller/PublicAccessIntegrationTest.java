package com.assetmarket.api.controller;

import com.assetmarket.api.dto.OnboardingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PublicAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowPublicReadWithTenantHeader() throws Exception {
        // Use shorter unique ID to satisfy @Size(max = 20) constraints
        String tenantSlug = "pub" + (System.currentTimeMillis() % 1000000);

        // 1. Setup: Create a tenant and a property
        OnboardingRequest onboardingRequest = OnboardingRequest.builder()
                .companyName("Public Corp " + tenantSlug)
                .slug(tenantSlug)
                .adminUsername("adm" + tenantSlug)
                .adminEmail(tenantSlug + "@example.com")
                .adminPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/onboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(onboardingRequest)))
                .andExpect(status().isOk());

        // 2. Test: Anonymous GET with X-Tenant-ID header
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantSlug))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailPublicReadWithoutTenantHeader() throws Exception {
        // Anonymous GET without header should fail with 400 (Bad Request) as per our
        // TenantFilter
        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBlockPublicWriteEvenWithTenantHeader() throws Exception {
        String tenantSlug = "write-block-test";

        // Anonymous POST even with header should fail with 403 (Forbidden)
        // by default in Spring Security for unauthenticated requests to protected paths
        mockMvc.perform(post("/api/v1/properties")
                .header("X-Tenant-ID", tenantSlug)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
