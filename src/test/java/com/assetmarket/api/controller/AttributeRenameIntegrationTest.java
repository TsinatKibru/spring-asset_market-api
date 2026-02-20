package com.assetmarket.api.controller;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.entity.Role;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AttributeRenameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tenantId = "rename-test-tenant";
    private Category testCategory;
    private Property testProperty;

    @BeforeEach
    public void setup() {
        TenantContext.setCurrentTenant(tenantId);

        // Create admin user
        User admin = new User();
        admin.setUsername("rename-admin");
        admin.setEmail("rename@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRoles(Set.of(Role.ROLE_ADMIN));
        admin.setTenantId(tenantId);
        userRepository.save(admin);

        // Create initial category with "old_name"
        Map<String, Object> oldField = new HashMap<>();
        oldField.put("name", "old_name");
        oldField.put("type", "number");
        oldField.put("required", true);

        testCategory = Category.builder()
                .name("RenameTest")
                .description("Testing renames")
                .attributeSchema(new ArrayList<>(List.of(oldField)))
                .tenantId(tenantId)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create property with "old_name"
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("old_name", 123);

        testProperty = Property.builder()
                .title("Rename Property")
                .description("Initial description")
                .price(new BigDecimal("100.00"))
                .location("Rename St")
                .category(testCategory)
                .attributes(attributes)
                .tenantId(tenantId)
                .build();
        testProperty = propertyRepository.save(testProperty);
    }

    @Test
    @WithMockUser(username = "rename-admin", roles = { "ADMIN" })
    public void shouldHandleAttributeRenameGracefully() throws Exception {
        // 1. Update Category Schema: rename "old_name" to "new_name"
        Map<String, Object> newField = new HashMap<>();
        newField.put("name", "new_name");
        newField.put("type", "number");
        newField.put("required", true);

        testCategory.setAttributeSchema(new ArrayList<>(List.of(newField)));
        categoryRepository.saveAndFlush(testCategory);

        // 2. Attempt to update property using the NEW schema
        // Note: The property in DB still has {"old_name": 123}

        PropertyDTO updateDTO = new PropertyDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPrice(new BigDecimal("150.00"));
        updateDTO.setLocation("Rename St");
        updateDTO.setCategoryName("RenameTest");

        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("new_name", 456);
        // We are NOT sending "old_name" here.
        updateDTO.setAttributes(newAttributes);

        mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes.new_name").value(456))
                .andExpect(jsonPath("$.attributes.old_name").doesNotExist());

        // 3. Verify that we can update even if we send the old key (it should be
        // sanitized/discarded)
        Map<String, Object> staleAttributes = new HashMap<>();
        staleAttributes.put("new_name", 789);
        staleAttributes.put("old_name", 123); // STALE KEY
        updateDTO.setAttributes(staleAttributes);

        mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes.new_name").value(789))
                .andExpect(jsonPath("$.attributes.old_name").doesNotExist());
    }
}
