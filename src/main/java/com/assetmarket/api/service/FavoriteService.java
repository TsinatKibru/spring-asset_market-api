package com.assetmarket.api.service;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Favorite;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.FavoriteRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyService propertyService;

    @Transactional
    public String toggleFavorite(Long propertyId) {
        User user = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // Ensure property belongs to same tenant
        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndProperty(user, property);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            return "Property removed from favorites";
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .property(property)
                    .tenantId(TenantContext.getCurrentTenant())
                    .build();
            favoriteRepository.save(favorite);
            return "Property added to favorites";
        }
    }

    @Transactional(readOnly = true)
    public Page<PropertyDTO> getSavedProperties(Pageable pageable) {
        User user = getCurrentUser();
        Page<Property> properties = favoriteRepository.findByUserId(user.getId(), pageable);
        return properties.map(propertyService::convertToDTO);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsernameAndTenantId(username, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalStateException("User context not found"));
    }
}
