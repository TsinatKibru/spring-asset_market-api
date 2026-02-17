package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
        Page<Property> findByCategoryName(String name, Pageable pageable);

        @Query(value = "SELECT * FROM properties p WHERE " +
                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
                        "(:location IS NULL OR p.location ILIKE CONCAT('%', :location, '%')) AND " +
                        "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
                        "(:status IS NULL OR p.status = :status) AND " +
                        "(:attrKey IS NULL OR p.attributes->>:attrKey = :attrValue) AND " +
                        "p.tenant_id = :tenantId", countQuery = "SELECT count(*) FROM properties p WHERE " +
                                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                                        "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
                                        "(:location IS NULL OR p.location ILIKE CONCAT('%', :location, '%')) AND " +
                                        "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
                                        "(:status IS NULL OR p.status = :status) AND " +
                                        "(:attrKey IS NULL OR p.attributes->>:attrKey = :attrValue) AND " +
                                        "p.tenant_id = :tenantId", nativeQuery = true)
        Page<Property> findWithFilters(
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("location") String location,
                        @Param("categoryId") Long categoryId,
                        @Param("status") String status,
                        @Param("attrKey") String attrKey,
                        @Param("attrValue") String attrValue,
                        @Param("tenantId") String tenantId,
                        Pageable pageable);
}
