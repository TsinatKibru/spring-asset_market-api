package com.assetmarket.api.repository;

import com.assetmarket.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndTenantId(String username, String tenantId);

    Boolean existsByUsernameAndTenantId(String username, String tenantId);

    Boolean existsByEmailAndTenantId(String email, String tenantId);
}
