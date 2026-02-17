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
public class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantId = "test-tenant-msg";
    private Long propertyId;

    @BeforeEach
    public void setup() {
        messageRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.builder().name("Msg Tenant").slug(tenantId).active(true).build();
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
                .title("Inquiry Property")
                .price(new BigDecimal("100000"))
                .location("Test City")
                .tenantId(tenantId)
                .build();
        p = propertyRepository.save(p);
        propertyId = p.getId();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldSendInquiryAndRetrieveThread() throws Exception {
        // 1. Send inquiry
        Map<String, Object> request = Map.of(
                "propertyId", propertyId,
                "content", "I am interested in this property");

        mockMvc.perform(post("/api/v1/messages/inquiry")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("I am interested in this property")))
                .andExpect(jsonPath("$.senderUsername", is("testuser")));

        // 2. Retrieve thread
        mockMvc.perform(get("/api/v1/messages/thread/{id}", propertyId)
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].senderUsername", is("testuser")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void adminShouldReplyAndUserShouldSee() throws Exception {
        // Setup: User inquiry exists
        User user = userRepository.findByUsernameAndTenantId("testuser", tenantId).get();
        Property property = propertyRepository.findById(propertyId).get();

        messageRepository.save(Message.builder()
                .property(property)
                .sender(user)
                .content("User Inquiry")
                .tenantId(tenantId)
                .build());

        // 1. Admin replies
        Map<String, Object> replyRequest = Map.of(
                "propertyId", propertyId,
                "content", "Hello, how can I help?");

        mockMvc.perform(post("/api/v1/messages/inquiry")
                .header("X-Tenant-ID", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderUsername", is("admin")));

        // 2. Verify thread contains both (as admin)
        mockMvc.perform(get("/api/v1/messages/thread/{id}", propertyId)
                .param("userId", user.getId().toString())
                .header("X-Tenant-ID", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void shouldNotAccessCrossTenantProperty() throws Exception {
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

        Map<String, Object> request = Map.of(
                "propertyId", p2.getId(),
                "content", "Hacker content");

        mockMvc.perform(post("/api/v1/messages/inquiry")
                .header("X-Tenant-ID", tenantId) // My tenant
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
