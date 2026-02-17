package com.assetmarket.api.controller;

import com.assetmarket.api.entity.*;
import com.assetmarket.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FavoriteIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PropertyRepository propertyRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private TenantRepository tenantRepository;

        @Autowired
        private FavoriteRepository favoriteRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String tenantId = "test-tenant-favs";
        private Long propertyId;
        private String username = "testuser";

        @BeforeEach
        public void setup() {
                favoriteRepository.deleteAll();
                propertyRepository.deleteAll();
                userRepository.deleteAll();
                categoryRepository.deleteAll();
                tenantRepository.deleteAll();

                Tenant tenant = Tenant.builder()
                                .name("Fav Tenant")
                                .slug(tenantId)
                                .active(true)
                                .build();
                tenantRepository.save(tenant);

                User user = User.builder()
                                .username(username)
                                .email("user@test.com")
                                .password(passwordEncoder.encode("password"))
                                .roles(Set.of(Role.ROLE_USER))
                                .tenantId(tenantId)
                                .build();
                userRepository.save(user);

                Category residential = Category.builder()
                                .name("Residential")
                                .description("Housing")
                                .tenantId(tenantId)
                                .attributeSchema(
                                                List.of(Map.of("name", "bedrooms", "type", "number", "required", true)))
                                .build();
                categoryRepository.save(residential);

                Property p = Property.builder()
                                .title("Nice Apartment")
                                .price(new BigDecimal("300000"))
                                .location("Downtown")
                                .category(residential)
                                .tenantId(tenantId)
                                .attributes(Map.of("bedrooms", 2))
                                .build();
                p = propertyRepository.save(p);
                propertyId = p.getId();
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        public void shouldToggleFavoriteSuccessfully() throws Exception {
                // 1. Add to favorites
                mockMvc.perform(post("/api/v1/favorites/{id}", propertyId)
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", containsString("added to favorites")));

                // 2. Check favorites list
                mockMvc.perform(get("/api/v1/favorites")
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].id", is(propertyId.intValue())));

                // 3. Remove from favorites
                mockMvc.perform(post("/api/v1/favorites/{id}", propertyId)
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", containsString("removed from favorites")));

                // 4. Verify empty list
                mockMvc.perform(get("/api/v1/favorites")
                                .header("X-Tenant-ID", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        public void shouldNotFavoriteCrossTenant() throws Exception {
                // Create another tenant and property
                String otherTenant = "other-tenant";
                Tenant t2 = Tenant.builder().name("T2").slug(otherTenant).active(true).build();
                tenantRepository.save(t2);

                Property p2 = Property.builder()
                                .title("Foreign Property")
                                .price(new BigDecimal("999999"))
                                .location("Other Land")
                                .tenantId(otherTenant)
                                .build();
                p2 = propertyRepository.save(p2);

                // Attempting to favorite property from other tenant should fail (due to tenant
                // filter or explicit check)
                mockMvc.perform(post("/api/v1/favorites/{id}", p2.getId())
                                .header("X-Tenant-ID", tenantId)) // Sending current tenant header
                                .andExpect(status().isBadRequest());
        }
}
