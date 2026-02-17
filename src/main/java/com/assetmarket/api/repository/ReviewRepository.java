package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByPropertyAndTenantId(Property property, String tenantId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.property.id = :propertyId")
    Double getAverageRatingForProperty(Long propertyId);

    Long countByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndUserId(Long propertyId, Long userId);
}
