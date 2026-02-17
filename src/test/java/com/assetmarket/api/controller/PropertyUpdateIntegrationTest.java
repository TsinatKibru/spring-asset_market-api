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
public class PropertyUpdateIntegrationTest {

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

        private Category residentialCategory;
        private Category commercialCategory;
        private Property testProperty;
        private String tenantId = "test-tenant";
        private String otherTenantId = "other-tenant";

        @BeforeEach
        public void setup() {
                TenantContext.setCurrentTenant(tenantId);

                // Create admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@test.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setRoles(Set.of(Role.ROLE_ADMIN));
                admin.setTenantId(tenantId);
                userRepository.save(admin);

                // Create regular user for authorization tests
                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setEmail("user@test.com");
                regularUser.setPassword(passwordEncoder.encode("password"));
                regularUser.setRoles(Set.of(Role.ROLE_USER));
                regularUser.setTenantId(tenantId);
                userRepository.save(regularUser);

                // Create Residential category
                Map<String, Object> bedroomsSchema = new HashMap<>();
                bedroomsSchema.put("name", "bedrooms");
                bedroomsSchema.put("type", "number");
                bedroomsSchema.put("required", true);

                Map<String, Object> bathroomsSchema = new HashMap<>();
                bathroomsSchema.put("name", "bathrooms");
                bathroomsSchema.put("type", "number");
                bathroomsSchema.put("required", true);

                residentialCategory = Category.builder()
                                .name("Residential")
                                .description("Housing properties")
                                .attributeSchema(List.of(bedroomsSchema, bathroomsSchema))
                                .tenantId(tenantId)
                                .build();
                residentialCategory = categoryRepository.save(residentialCategory);

                // Create Commercial category
                Map<String, Object> zoningSchema = new HashMap<>();
                zoningSchema.put("name", "zoningCode");
                zoningSchema.put("type", "string");
                zoningSchema.put("required", true);

                commercialCategory = Category.builder()
                                .name("Commercial")
                                .description("Business properties")
                                .attributeSchema(List.of(zoningSchema))
                                .tenantId(tenantId)
                                .build();
                commercialCategory = categoryRepository.save(commercialCategory);

                // Create test property
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 3);
                attributes.put("bathrooms", 2);

                testProperty = Property.builder()
                                .title("Test House")
                                .description("Original description")
                                .price(new BigDecimal("300000.00"))
                                .location("123 Test St")
                                .category(residentialCategory)
                                .attributes(attributes)
                                .tenantId(tenantId)
                                .build();
                testProperty = propertyRepository.save(testProperty);
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldUpdatePropertySuccessfully() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Updated House");
                updateDTO.setDescription("Updated description");
                updateDTO.setPrice(new BigDecimal("350000.00"));
                updateDTO.setLocation("456 New St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> newAttributes = new HashMap<>();
                newAttributes.put("bedrooms", 4);
                newAttributes.put("bathrooms", 3);
                updateDTO.setAttributes(newAttributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated House"))
                                .andExpect(jsonPath("$.description").value("Updated description"))
                                .andExpect(jsonPath("$.price").value(350000.00))
                                .andExpect(jsonPath("$.location").value("456 New St"))
                                .andExpect(jsonPath("$.attributes.bedrooms").value(4))
                                .andExpect(jsonPath("$.attributes.bathrooms").value(3));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldChangeCategorySuccessfully() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Converted to Office");
                updateDTO.setDescription("Now commercial");
                updateDTO.setPrice(new BigDecimal("400000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Commercial");

                Map<String, Object> newAttributes = new HashMap<>();
                newAttributes.put("zoningCode", "C-1");
                updateDTO.setAttributes(newAttributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.categoryName").value("Commercial"))
                                .andExpect(jsonPath("$.attributes.zoningCode").value("C-1"))
                                .andExpect(jsonPath("$.attributes.bedrooms").doesNotExist());
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldRejectUpdateWithMissingRequiredAttribute() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Invalid Update");
                updateDTO.setDescription("Missing bathrooms");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> invalidAttributes = new HashMap<>();
                invalidAttributes.put("bedrooms", 3);
                // Missing required "bathrooms"
                updateDTO.setAttributes(invalidAttributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("bathrooms")));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldRejectUpdateWithInvalidAttributeType() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Invalid Type");
                updateDTO.setDescription("Wrong type");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> invalidAttributes = new HashMap<>();
                invalidAttributes.put("bedrooms", "three"); // Should be number
                invalidAttributes.put("bathrooms", 2);
                updateDTO.setAttributes(invalidAttributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("bedrooms")));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldRejectUpdateOfNonExistentProperty() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Non-existent");
                updateDTO.setDescription("Should fail");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 3);
                attributes.put("bathrooms", 2);
                updateDTO.setAttributes(attributes);

                mockMvc.perform(put("/api/v1/properties/{id}", 99999L)
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("Property not found")));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldRejectCrossTenantUpdate() throws Exception {
                // Create property in different tenant
                TenantContext.setCurrentTenant(otherTenantId);

                Category otherCategory = Category.builder()
                                .name("Residential")
                                .description("Other tenant category")
                                .attributeSchema(residentialCategory.getAttributeSchema())
                                .tenantId(otherTenantId)
                                .build();
                otherCategory = categoryRepository.save(otherCategory);

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 2);
                attributes.put("bathrooms", 1);

                Property otherProperty = Property.builder()
                                .title("Other Tenant House")
                                .description("Different tenant")
                                .price(new BigDecimal("200000.00"))
                                .location("999 Other St")
                                .category(otherCategory)
                                .attributes(attributes)
                                .tenantId(otherTenantId)
                                .build();
                otherProperty = propertyRepository.save(otherProperty);

                // Try to update from original tenant
                TenantContext.setCurrentTenant(tenantId);

                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Hacked Update");
                updateDTO.setDescription("Should not work");
                updateDTO.setPrice(new BigDecimal("1.00"));
                updateDTO.setLocation("Hacker St");
                updateDTO.setCategoryName("Residential");
                updateDTO.setAttributes(attributes);

                mockMvc.perform(put("/api/v1/properties/{id}", otherProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("not found")));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldRejectUpdateToNonExistentCategory() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Invalid Category");
                updateDTO.setDescription("Bad category");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("NonExistent");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 3);
                attributes.put("bathrooms", 2);
                updateDTO.setAttributes(attributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("Category not found")));
        }

        @Test
        public void shouldRequireAuthentication() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Unauthorized");
                updateDTO.setDescription("Should fail");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 3);
                attributes.put("bathrooms", 2);
                updateDTO.setAttributes(attributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isForbidden()); // Spring Security returns 403 for anonymous users
        }

        @Test
        @WithMockUser(username = "user", roles = { "USER" })
        public void shouldRequireAdminRole() throws Exception {
                PropertyDTO updateDTO = new PropertyDTO();
                updateDTO.setTitle("Forbidden");
                updateDTO.setDescription("Should fail");
                updateDTO.setPrice(new BigDecimal("300000.00"));
                updateDTO.setLocation("123 Test St");
                updateDTO.setCategoryName("Residential");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", 3);
                attributes.put("bathrooms", 2);
                updateDTO.setAttributes(attributes);

                mockMvc.perform(put("/api/v1/properties/{id}", testProperty.getId())
                                .header("X-Tenant-ID", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isForbidden());
        }
}
