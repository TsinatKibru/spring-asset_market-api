package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Favorite;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndProperty(User user, Property property);

    boolean existsByUserAndProperty(User user, Property property);

    @Query("SELECT f.property FROM Favorite f WHERE f.user.id = :userId")
    Page<Property> findByUserId(@Param("userId") Long userId, Pageable pageable);

    void deleteByUserAndProperty(User user, Property property);
}
