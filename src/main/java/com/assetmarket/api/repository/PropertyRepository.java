package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByCategoryName(String name, Pageable pageable);
}
