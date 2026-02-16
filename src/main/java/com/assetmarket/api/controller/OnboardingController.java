package com.assetmarket.api.controller;

import com.assetmarket.api.dto.OnboardingRequest;
import com.assetmarket.api.entity.Role;
import com.assetmarket.api.entity.Tenant;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.TenantRepository;
import com.assetmarket.api.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/onboard")
@Tag(name = "SaaS Onboarding", description = "Endpoints for creating new organizations and their first administrators")
public class OnboardingController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping
    @Transactional
    @Operation(summary = "Onboard a new company", description = "Creates a new organization and its first administrator in a single transactional step")
    public ResponseEntity<?> onboardCompany(@Valid @RequestBody OnboardingRequest request) {
        // 1. Check for duplicates
        if (tenantRepository.existsBySlug(request.getSlug())) {
            return ResponseEntity.badRequest().body("Error: Slug is already taken!");
        }
        if (tenantRepository.existsByName(request.getCompanyName())) {
            return ResponseEntity.badRequest().body("Error: Company name is already in use!");
        }
        if (userRepository.existsByUsername(request.getAdminUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // 2. Create Tenant
        Tenant tenant = Tenant.builder()
                .slug(request.getSlug())
                .name(request.getCompanyName())
                .active(true)
                .build();
        tenantRepository.save(tenant);

        // 3. Create Admin User
        User admin = User.builder()
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .password(encoder.encode(request.getAdminPassword()))
                .tenantId(tenant.getSlug())
                .roles(Collections.singleton(Role.ROLE_ADMIN))
                .build();
        userRepository.save(admin);

        return ResponseEntity
                .ok("Organization and Admin created successfully! You can now login using the admin credentials.");
    }
}
