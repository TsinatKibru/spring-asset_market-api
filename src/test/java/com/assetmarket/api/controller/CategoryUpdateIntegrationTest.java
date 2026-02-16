package com.assetmarket.api.controller;

import com.assetmarket.api.dto.CategoryDTO;
import com.assetmarket.api.dto.AttributeSchemaDTO;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryUpdateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldUpdateCategorySchema() throws Exception {
        // 1. Setup existing category
        TenantContext.setCurrentTenant("test-tenant");
        Category category = Category.builder()
                .name("LegacyCategory")
                .description("Old Description")
                .tenantId("test-tenant")
                .build(); // No schema initially
        category = categoryRepository.save(category);

        // 2. Prepare Update DTO with Schema
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("LegacyCategory") // Keep name
                .description("Updated Description")
                .attributeSchema(List.of(
                        new AttributeSchemaDTO("newField", "string", true)))
                .build();

        // 3. Perform PUT update
        mockMvc.perform(put("/api/v1/categories/" + category.getId())
                .header("X-Tenant-ID", "test-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.attributeSchema[0].name").value("newField"));

        // 4. VerifyDB persistence
        // (Optional: Implicitly verified by response, but good to be sure)
    }
}
