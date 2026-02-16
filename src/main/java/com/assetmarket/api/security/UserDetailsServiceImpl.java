package com.assetmarket.api.security;

import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
        @Autowired
        UserRepository userRepository;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                String tenantId = TenantContext.getCurrentTenant();
                if (tenantId == null) {
                        throw new UsernameNotFoundException(
                                        "Tenant context missing. X-Tenant-ID header required for authentication.");
                }

                User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("User %s not found in tenant %s", username, tenantId)));

                return new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(role.name()))
                                                .collect(Collectors.toList()));
        }
}
