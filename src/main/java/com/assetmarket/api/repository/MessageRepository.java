package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Message;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages for a property thread between a user and admins
    @Query("SELECT m FROM Message m JOIN m.sender s WHERE m.property.id = :propertyId AND (s.id = :userId OR com.assetmarket.api.entity.Role.ROLE_ADMIN MEMBER OF s.roles) AND m.tenantId = :tenantId ORDER BY m.createdAt ASC")
    Page<Message> findThread(@Param("propertyId") Long propertyId, @Param("userId") Long userId,
            @Param("tenantId") String tenantId, Pageable pageable);

    // List all unique properties a user has inquired about
    @Query("SELECT DISTINCT m.property FROM Message m WHERE m.sender.id = :userId AND m.tenantId = :tenantId")
    Page<Property> findInquiredProperties(@Param("userId") Long userId, @Param("tenantId") String tenantId,
            Pageable pageable);

    // List all inquiries for a property (Admin view)
    @Query("SELECT m FROM Message m WHERE m.property.id = :propertyId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<Message> findByPropertyAndTenantId(@Param("propertyId") Long propertyId, @Param("tenantId") String tenantId,
            Pageable pageable);
}
