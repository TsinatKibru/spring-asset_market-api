package com.assetmarket.api.controller;

import com.assetmarket.api.dto.JwtResponse;
import com.assetmarket.api.dto.LoginRequest;
import com.assetmarket.api.dto.SignupRequest;
import com.assetmarket.api.dto.UserResponseDTO;
import com.assetmarket.api.entity.Role;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "X-Tenant-ID", required = true) String tenantIdHeader) {

        if (tenantIdHeader == null || tenantIdHeader.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: X-Tenant-ID header is required");
        }
        com.assetmarket.api.security.TenantContext.setCurrentTenant(tenantIdHeader);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authentication
                    .getPrincipal();

            User user = userRepository.findByUsernameAndTenantId(userDetails.getUsername(),
                    com.assetmarket.api.security.TenantContext.getCurrentTenant()).get();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getTenantId(),
                    roles));
        } finally {
            com.assetmarket.api.security.TenantContext.clear();
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentTenant = com.assetmarket.api.security.TenantContext.getCurrentTenant();

        User user = userRepository.findByUsernameAndTenantId(username, currentTenant)
                .orElseThrow(() -> new RuntimeException("User not found"));

        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
        log.info("Fetching profile for user: {} in tenant: {}. Roles: {}",
                user.getUsername(), user.getTenantId(), user.getRoles());

        return ResponseEntity.ok(UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .build());
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Get current admin's tenant ID
        String currentTenant = com.assetmarket.api.security.TenantContext.getCurrentTenant();

        if (userRepository.existsByUsernameAndTenantId(signUpRequest.getUsername(), currentTenant)) {
            return ResponseEntity.badRequest().body("Error: Username is already taken in this tenant!");
        }

        if (userRepository.existsByEmailAndTenantId(signUpRequest.getEmail(), currentTenant)) {
            return ResponseEntity.badRequest().body("Error: Email is already in use in this tenant!");
        }

        // Create new user's account with the same tenantId as the admin
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .tenantId(currentTenant)
                .build();

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(Role.ROLE_USER);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        roles.add(Role.ROLE_ADMIN);
                        break;
                    default:
                        roles.add(Role.ROLE_USER);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}
