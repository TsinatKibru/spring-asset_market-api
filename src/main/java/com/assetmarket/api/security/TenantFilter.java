package com.assetmarket.api.security;

import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = null;

        // 1. Try to get tenantId from Authenticated Principal (JWT)
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (principal instanceof UserDetails userDetails) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                tenantId = user.getTenantId();
            }
        }

        // 2. Fallback to Header if unauthenticated (Public Access)
        if (tenantId == null) {
            tenantId = request.getHeader("X-Tenant-ID");
        }

        // 3. Validation: If accessing property/category data, tenantId MUST be present
        String path = request.getRequestURI();
        if (tenantId == null && (path.startsWith("/api/v1/properties") || path.startsWith("/api/v1/categories"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Tenant Identification (JWT or X-Tenant-ID header) is required.");
            return;
        }

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
