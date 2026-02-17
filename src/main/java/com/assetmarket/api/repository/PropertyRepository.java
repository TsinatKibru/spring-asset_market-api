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

        @Query("SELECT p FROM Property p WHERE " +
                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
                        "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%'))) AND "
                        +
                        "(:categoryId IS NULL OR p.category.id = :categoryId)")
        Page<Property> findWithFilters(
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("location") String location,
                        @Param("categoryId") Long categoryId,
                        Pageable pageable);
}
