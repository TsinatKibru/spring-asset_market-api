package com.assetmarket.api.controller;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Search properties with filters and sorting", description = "Search properties by price range, location, category with sorting options")
    public ResponseEntity<Page<PropertyDTO>> getAllProperties(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Build Sort object
        org.springframework.data.domain.Sort.Direction direction = sortDir.equalsIgnoreCase("ASC")
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        // Use search method if any filters are provided
        Page<PropertyDTO> properties;
        if (minPrice != null || maxPrice != null || location != null) {
            properties = propertyService.searchProperties(
                    minPrice, maxPrice, location, category, pageable);
        } else {
            properties = propertyService.getAllProperties(category, pageable);
        }

        return ResponseEntity.ok(properties);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> createProperty(@Valid @RequestBody PropertyDTO propertyDTO) {
        return new ResponseEntity<>(propertyService.createProperty(propertyDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id,
            @Valid @RequestBody PropertyDTO propertyDTO) {
        return ResponseEntity.ok(propertyService.updateProperty(id, propertyDTO));
    }
}
