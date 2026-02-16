package com.assetmarket.api.controller;

import com.assetmarket.api.dto.AttributeSchemaDTO;
import com.assetmarket.api.dto.CategoryDTO;
import com.assetmarket.api.dto.PropertyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MetadataValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_TENANT = "test-tenant";

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldFailWhenRequiredMetadataIsMissing() throws Exception {
        // 1. Create a category with a schema requiring "rooms"
        AttributeSchemaDTO field = new AttributeSchemaDTO("rooms", "number", true);
        CategoryDTO category = CategoryDTO.builder()
                .name("StrictCategory")
                .attributeSchema(List.of(field))
                .build();

        mockMvc.perform(post("/api/v1/categories")
                .header("X-Tenant-ID", TEST_TENANT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated());

        // 2. Try to create a property without "rooms"
        Map<String, Object> props = new HashMap<>(); // Empty attributes
        PropertyDTO property = new PropertyDTO();
        property.setTitle("Invalid House");
        property.setPrice(new java.math.BigDecimal(100000));
        property.setLocation("Validation City");
        property.setCategoryName("StrictCategory");
        property.setAttributes(props);

        mockMvc.perform(post("/api/v1/properties")
                .header("X-Tenant-ID", TEST_TENANT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(property)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldFailWhenMetadataTypeIsInvalid() throws Exception {
        // 1. Create a category requiring "rooms" as number
        AttributeSchemaDTO field = new AttributeSchemaDTO("rooms", "number", true);
        CategoryDTO category = CategoryDTO.builder()
                .name("TypeCategory")
                .attributeSchema(List.of(field))
                .build();

        mockMvc.perform(post("/api/v1/categories")
                .header("X-Tenant-ID", TEST_TENANT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated());

        // 2. Try to create a property with "rooms" as string
        Map<String, Object> props = new HashMap<>();
        props.put("rooms", "three"); // Invalid Type

        PropertyDTO property = new PropertyDTO();
        property.setTitle("Invalid Type House");
        property.setPrice(new java.math.BigDecimal(200000));
        property.setLocation("Type City");
        property.setCategoryName("TypeCategory");
        property.setAttributes(props);

        mockMvc.perform(post("/api/v1/properties")
                .header("X-Tenant-ID", TEST_TENANT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(property)))
                .andExpect(status().isBadRequest());
    }
}
