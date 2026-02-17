package com.assetmarket.api.controller;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.PropertyStatus;
import com.assetmarket.api.entity.Tenant;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PropertyStatusAndAttributeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantId = "test-tenant-status";

    @BeforeEach
    public void setup() {
        propertyRepository.deleteAll();
        categoryRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.builder()
                .name("Status Tenant")
                .slug(tenantId)
                .active(true)
                .build();
        tenantRepository.save(tenant);

        Map<String, Object> residentialField1 = new HashMap<>();
        residentialField1.put("name", "bedrooms");
        residentialField1.put("type", "number");
        residentialField1.put("required", true);

        Category residential = Category.builder()
                .name("Residential")
                .description("Houses and apartments")
                .tenantId(tenantId)
                .attributeSchema(List.of(residentialField1))
                .build();
        categoryRepository.save(residential);

        // Property 1: Available, 3 bedrooms
        Property p1 = Property.builder()
                .title("P1 Available 3BR")
                .price(new BigDecimal("100000"))
                .location("Loc 1")
                .category(residential)
                .status(PropertyStatus.AVAILABLE)
                .attributes(Map.of("bedrooms", 3))
                .tenantId(tenantId)
                .build();

        // Property 2: Pending, 3 bedrooms
        Property p2 = Property.builder()
                .title("P2 Pending 3BR")
                .price(new BigDecimal("200000"))
                .location("Loc 2")
                .category(residential)
                .status(PropertyStatus.PENDING)
                .attributes(Map.of("bedrooms", 3))
                .tenantId(tenantId)
                .build();

        // Property 3: Sold, 4 bedrooms
        Property p3 = Property.builder()
                .title("P3 Sold 4BR")
                .price(new BigDecimal("300000"))
                .location("Loc 3")
                .category(residential)
                .status(PropertyStatus.SOLD)
                .attributes(Map.of("bedrooms", 4))
                .tenantId(tenantId)
                .build();

        propertyRepository.saveAll(List.of(p1, p2, p3));
    }

    @Test
    public void shouldFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("P1 Available 3BR"));

        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("status", "SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("P3 Sold 4BR"));
    }

    @Test
    public void shouldFilterByAttribute() throws Exception {
        // Search for 3 bedrooms
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("attrKey", "bedrooms")
                .param("attrValue", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Search for 4 bedrooms
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("attrKey", "bedrooms")
                .param("attrValue", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("P3 Sold 4BR"));
    }

    @Test
    public void shouldCombineStatusAndAttributeFilter() throws Exception {
        // Pending AND 3 bedrooms
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("status", "PENDING")
                .param("attrKey", "bedrooms")
                .param("attrValue", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("P2 Pending 3BR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldUpdateStatus() throws Exception {
        Property p = propertyRepository.findAll().stream()
                .filter(prop -> prop.getTitle().equals("P1 Available 3BR"))
                .findFirst().get();

        PropertyDTO updateDTO = new PropertyDTO();
        updateDTO.setTitle("P1 Updated to Sold");
        updateDTO.setPrice(p.getPrice());
        updateDTO.setLocation(p.getLocation());
        updateDTO.setCategoryName("Residential");
        updateDTO.setStatus(PropertyStatus.SOLD);
        updateDTO.setAttributes(p.getAttributes());

        mockMvc.perform(put("/api/v1/properties/{id}", p.getId())
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));

        // Verify filter picks it up
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId)
                .param("status", "SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }
}
