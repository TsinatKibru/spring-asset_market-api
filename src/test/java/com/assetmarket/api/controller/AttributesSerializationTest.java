package com.assetmarket.api.controller;

import com.assetmarket.api.dto.CategoryDTO;
import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.dto.AttributeSchemaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AttributesSerializationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldSerializeAttributesInResponse() throws Exception {
        // 1. Create Category with Schema
        CategoryDTO category = CategoryDTO.builder()
                .name("SerializationTestCat")
                .description("Test Category")
                .attributeSchema(List.of(
                        new AttributeSchemaDTO("size", "number", true),
                        new AttributeSchemaDTO("color", "string", false)))
                .build();

        mockMvc.perform(post("/api/v1/categories")
                .header("X-Tenant-ID", "test-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated());

        // 2. Create Property
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("size", 100);
        attrs.put("color", "red");

        PropertyDTO property = new PropertyDTO();
        property.setTitle("Test Property");
        property.setPrice(new BigDecimal("100.00"));
        property.setLocation("Test Location");
        property.setCategoryName("SerializationTestCat");
        property.setAttributes(attrs);

        // 3. Verify Response contains attributes
        mockMvc.perform(post("/api/v1/properties")
                .header("X-Tenant-ID", "test-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(property)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attributes").exists())
                .andExpect(jsonPath("$.attributes.size").value(100))
                .andExpect(jsonPath("$.attributes.color").value("red"));
    }
}
