package com.assetmarket.api.controller;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Endpoints for managing user's saved properties")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle property favorite status", description = "Adds property to favorites if not present, removes it otherwise")
    public ResponseEntity<Map<String, String>> toggleFavorite(@PathVariable Long propertyId) {
        String message = favoriteService.toggleFavorite(propertyId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "List saved properties", description = "Get a paginated list of properties favorited by the current user")
    public ResponseEntity<Page<PropertyDTO>> getSavedProperties(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(favoriteService.getSavedProperties(pageable));
    }
}
