package com.assetmarket.api.controller;

import com.assetmarket.api.entity.*;
import com.assetmarket.api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantId = "test-tenant-reviews";
    private Long propertyId;

    @BeforeEach
    public void setup() {
        reviewRepository.deleteAll();
        messageRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.builder().name("Reviews Tenant").slug(tenantId).active(true).build();
        tenantRepository.save(tenant);

        User user = User.builder()
                .username("testuser")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.ROLE_USER))
                .tenantId(tenantId)
                .build();
        userRepository.save(user);

        Property p = Property.builder()
                .title("Rated Property")
                .price(new BigDecimal("200000"))
                .location("Feedback City")
                .tenantId(tenantId)
                .build();
        p = propertyRepository.save(p);
        propertyId = p.getId();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldBlockUnverifiedUserFromReviewing() throws Exception {
        Map<String, Object> reviewRequest = Map.of(
                "propertyId", propertyId,
                "rating", 5,
                "comment", "This property is great!");

        mockMvc.perform(post("/api/v1/reviews")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Only verified users")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldAllowVerifiedUserToReviewAndAggregateRatings() throws Exception {
        User user = userRepository.findByUsernameAndTenantId("testuser", tenantId).get();
        Property property = propertyRepository.findById(propertyId).get();

        // 1. Create a verified interaction (Inquiry)
        messageRepository.save(Message.builder()
                .property(property)
                .sender(user)
                .content("I want to see this!")
                .tenantId(tenantId)
                .build());

        // 2. Submit Review
        Map<String, Object> reviewRequest = Map.of(
                "propertyId", propertyId,
                "rating", 5,
                "comment", "Amazing experience!");

        mockMvc.perform(post("/api/v1/reviews")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(5)));

        // 3. Verify aggregate ratings on property
        mockMvc.perform(get("/api/v1/properties")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].averageRating", is(5.0)))
                .andExpect(jsonPath("$.content[0].reviewCount", is(1)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldPreventMultipleReviews() throws Exception {
        User user = userRepository.findByUsernameAndTenantId("testuser", tenantId).get();
        Property property = propertyRepository.findById(propertyId).get();

        // Verified status
        messageRepository
                .save(Message.builder().property(property).sender(user).content("Hi").tenantId(tenantId).build());

        // First review
        reviewRepository.save(Review.builder()
                .property(property)
                .user(user)
                .rating(4)
                .tenantId(tenantId)
                .build());

        // Second review attempt
        Map<String, Object> reviewRequest = Map.of(
                "propertyId", propertyId,
                "rating", 1,
                "comment", "Just kidding");

        mockMvc.perform(post("/api/v1/reviews")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already reviewed")));
    }
}
