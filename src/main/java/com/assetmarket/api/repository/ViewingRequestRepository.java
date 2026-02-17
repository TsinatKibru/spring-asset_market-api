package com.assetmarket.api.repository;

import com.assetmarket.api.entity.User;
import com.assetmarket.api.entity.ViewingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewingRequestRepository extends JpaRepository<ViewingRequest, Long> {
    Page<ViewingRequest> findByUserAndTenantId(User user, String tenantId, Pageable pageable);

    Page<ViewingRequest> findByTenantId(String tenantId, Pageable pageable);
}
