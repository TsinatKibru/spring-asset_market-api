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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ViewingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ViewingRequestRepository viewingRequestRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantId = "test-tenant-viewing";
    private Long propertyId;

    @BeforeEach
    public void setup() {
        messageRepository.deleteAll();
        viewingRequestRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.builder().name("Viewing Tenant").slug(tenantId).active(true).build();
        tenantRepository.save(tenant);

        User user = User.builder()
                .username("testuser")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.ROLE_USER))
                .tenantId(tenantId)
                .build();
        userRepository.save(user);

        User admin = User.builder()
                .username("admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.ROLE_ADMIN))
                .tenantId(tenantId)
                .build();
        userRepository.save(admin);

        Property p = Property.builder()
                .title("Viewing Property")
                .price(new BigDecimal("100000"))
                .location("Appointment City")
                .tenantId(tenantId)
                .build();
        p = propertyRepository.save(p);
        propertyId = p.getId();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldRequestViewingAndTriggerSystemMessage() throws Exception {
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2).withNano(0);
        Map<String, Object> request = Map.of(
                "propertyId", propertyId,
                "requestedAt", appointmentTime.toString(),
                "notes", "I am very serious about this house");

        // 1. Request viewing
        mockMvc.perform(post("/api/v1/viewings/request")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.notes", is("I am very serious about this house")));

        // 2. Verify system message in chat
        mockMvc.perform(get("/api/v1/messages/thread/{id}", propertyId)
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", containsString("[SYSTEM] Viewing request")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void adminShouldApproveViewing() throws Exception {
        // Setup a pending viewing
        User user = userRepository.findByUsernameAndTenantId("testuser", tenantId).get();
        Property property = propertyRepository.findById(propertyId).get();

        ViewingRequest viewing = viewingRequestRepository.save(ViewingRequest.builder()
                .property(property)
                .user(user)
                .requestedAt(LocalDateTime.now().plusDays(1))
                .status(ViewingStatus.PENDING)
                .tenantId(tenantId)
                .build());

        // 1. Admin approves
        mockMvc.perform(patch("/api/v1/viewings/{id}/status", viewing.getId())
                .param("status", "APPROVED")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        // 2. Verify notification message
        mockMvc.perform(get("/api/v1/messages/thread/{id}", propertyId)
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", containsString("status updated to: APPROVED")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void userCannotApproveOwnViewing() throws Exception {
        User user = userRepository.findByUsernameAndTenantId("testuser", tenantId).get();
        Property property = propertyRepository.findById(propertyId).get();

        ViewingRequest viewing = viewingRequestRepository.save(ViewingRequest.builder()
                .property(property)
                .user(user)
                .requestedAt(LocalDateTime.now().plusDays(1))
                .status(ViewingStatus.PENDING)
                .tenantId(tenantId)
                .build());

        // User attempt to approve
        mockMvc.perform(patch("/api/v1/viewings/{id}/status", viewing.getId())
                .param("status", "APPROVED")
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isBadRequest());
    }
}
