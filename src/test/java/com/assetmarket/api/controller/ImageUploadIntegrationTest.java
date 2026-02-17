package com.assetmarket.api.controller;

import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.Tenant;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ImageUploadIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PropertyRepository propertyRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private TenantRepository tenantRepository;

        private String tenantId = "test-tenant-images";
        private Long propertyId;

        @BeforeEach
        public void setup() {
                propertyRepository.deleteAll();
                categoryRepository.deleteAll();
                tenantRepository.deleteAll();

                Tenant tenant = Tenant.builder()
                                .name("Image Tenant")
                                .slug(tenantId)
                                .active(true)
                                .build();
                tenantRepository.save(tenant);

                Category residential = Category.builder()
                                .name("Residential")
                                .description("Housing")
                                .tenantId(tenantId)
                                .attributeSchema(List.of(
                                                Map.of("name", "bedrooms", "type", "number", "required", true)))
                                .build();
                categoryRepository.save(residential);

                com.assetmarket.api.entity.Property p = com.assetmarket.api.entity.Property.builder()
                                .title("Property for Image Upload")
                                .price(new BigDecimal("500000"))
                                .location("Downtown")
                                .category(residential)
                                .tenantId(tenantId)
                                .attributes(Map.of("bedrooms", 3))
                                .build();
                p = propertyRepository.save(p);
                propertyId = p.getId();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        public void shouldUploadImageSuccessfully() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "test-image.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "fake-image-content".getBytes());

                mockMvc.perform(multipart("/api/v1/properties/{id}/images", propertyId)
                                .file(file)
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.imageUrls", hasSize(1)))
                                .andExpect(jsonPath("$.imageUrls[0]", containsString("/uploads/" + tenantId)))
                                .andExpect(jsonPath("$.imageUrls[0]", containsString(".jpg")));
        }
}
