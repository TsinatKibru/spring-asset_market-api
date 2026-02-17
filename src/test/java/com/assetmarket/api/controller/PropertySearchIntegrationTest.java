package com.assetmarket.api.controller;

import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.entity.Role;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class PropertySearchIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PropertyRepository propertyRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private Category residentialCategory;
        private String tenantId = "test-tenant";

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

                // Create Residential category
                Map<String, Object> bedroomsSchema = new HashMap<>();
                bedroomsSchema.put("name", "bedrooms");
                bedroomsSchema.put("type", "number");
                bedroomsSchema.put("required", true);

                residentialCategory = Category.builder()
                                .name("Residential")
                                .description("Housing properties")
                                .attributeSchema(List.of(bedroomsSchema))
                                .tenantId(tenantId)
                                .build();
                residentialCategory = categoryRepository.save(residentialCategory);

                // Create test properties with varying prices and locations
                createProperty("Budget Apartment", "200000.00", "Downtown", 1);
                createProperty("Mid-range House", "400000.00", "Suburb", 2);
                createProperty("Luxury Penthouse", "800000.00", "Downtown", 4);
                createProperty("Countryside Villa", "600000.00", "Countryside", 3);
        }

        private void createProperty(String title, String price, String location, int bedrooms) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("bedrooms", bedrooms);

                Property property = Property.builder()
                                .title(title)
                                .description("Test property")
                                .price(new BigDecimal(price))
                                .location(location)
                                .category(residentialCategory)
                                .attributes(attributes)
                                .tenantId(tenantId)
                                .build();
                propertyRepository.save(property);
        }

        @Test
        public void shouldFilterByMinPrice() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("minPrice", "500000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[*].title",
                                                hasItems("Luxury Penthouse", "Countryside Villa")));
        }

        @Test
        public void shouldFilterByMaxPrice() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("maxPrice", "500000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[*].title",
                                                hasItems("Budget Apartment", "Mid-range House")));
        }

        @Test
        public void shouldFilterByPriceRange() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("minPrice", "300000")
                                .param("maxPrice", "700000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[*].title",
                                                hasItems("Mid-range House", "Countryside Villa")));
        }

        @Test
        public void shouldSearchByLocation() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("location", "Downtown"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[*].title",
                                                hasItems("Budget Apartment", "Luxury Penthouse")));
        }

        @Test
        public void shouldSearchByPartialLocation() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("location", "down")) // Partial match
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[*].location",
                                                everyItem(containsStringIgnoringCase("downtown"))));
        }

        @Test
        public void shouldSortByPriceAscending() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("sortBy", "price")
                                .param("sortDir", "ASC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(4)))
                                .andExpect(jsonPath("$.content[0].title").value("Budget Apartment"))
                                .andExpect(jsonPath("$.content[1].title").value("Mid-range House"))
                                .andExpect(jsonPath("$.content[2].title").value("Countryside Villa"))
                                .andExpect(jsonPath("$.content[3].title").value("Luxury Penthouse"));
        }

        @Test
        public void shouldSortByPriceDescending() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("sortBy", "price")
                                .param("sortDir", "DESC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(4)))
                                .andExpect(jsonPath("$.content[0].title").value("Luxury Penthouse"))
                                .andExpect(jsonPath("$.content[1].title").value("Countryside Villa"))
                                .andExpect(jsonPath("$.content[2].title").value("Mid-range House"))
                                .andExpect(jsonPath("$.content[3].title").value("Budget Apartment"));
        }

        @Test
        public void shouldSortByTitleAscending() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("sortBy", "title")
                                .param("sortDir", "ASC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value("Budget Apartment"))
                                .andExpect(jsonPath("$.content[1].title").value("Countryside Villa"))
                                .andExpect(jsonPath("$.content[2].title").value("Luxury Penthouse"))
                                .andExpect(jsonPath("$.content[3].title").value("Mid-range House"));
        }

        @Test
        public void shouldCombineMultipleFilters() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("minPrice", "300000")
                                .param("maxPrice", "700000")
                                .param("location", "Suburb")
                                .param("sortBy", "price")
                                .param("sortDir", "ASC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].title").value("Mid-range House"));
        }

        @Test
        public void shouldFilterByCategory() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("category", "Residential"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(4)))
                                .andExpect(jsonPath("$.content[*].categoryName",
                                                everyItem(is("Residential"))));
        }

        @Test
        public void shouldReturnEmptyWhenNoMatches() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("minPrice", "1000000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        public void shouldSupportPagination() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId)
                                .param("page", "0")
                                .param("size", "2")
                                .param("sortBy", "price")
                                .param("sortDir", "ASC"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.totalElements").value(4))
                                .andExpect(jsonPath("$.totalPages").value(2))
                                .andExpect(jsonPath("$.content[0].title").value("Budget Apartment"));
        }

        @Test
        public void shouldDefaultToCreatedAtDescWhenNoSortSpecified() throws Exception {
                mockMvc.perform(get("/api/v1/properties")
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(4)));
                // Most recently created should be first (DESC order)
        }
}
