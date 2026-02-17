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
import com.assetmarket.api.security.TenantContext;

@RestController
@RequestMapping("/api/v1/properties")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Properties", description = "Endpoints for managing and searching assets")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private com.assetmarket.api.service.FileUploadService fileUploadService;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Search properties with filters and sorting", description = "Search properties by price range, location, category with sorting options")
    public ResponseEntity<Page<PropertyDTO>> getAllProperties(
            @io.swagger.v3.oas.annotations.Parameter(description = "Category name to filter by") @RequestParam(required = false) String category,
            @io.swagger.v3.oas.annotations.Parameter(description = "Minimum price") @RequestParam(required = false) java.math.BigDecimal minPrice,
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum price") @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @io.swagger.v3.oas.annotations.Parameter(description = "Partial location match") @RequestParam(required = false) String location,
            @io.swagger.v3.oas.annotations.Parameter(description = "Property status (AVAILABLE, PENDING, SOLD)") @RequestParam(required = false) com.assetmarket.api.entity.PropertyStatus status,
            @io.swagger.v3.oas.annotations.Parameter(description = "Dynamic attribute key (e.g., 'bedrooms')") @RequestParam(required = false) String attrKey,
            @io.swagger.v3.oas.annotations.Parameter(description = "Dynamic attribute value to match") @RequestParam(required = false) String attrValue,
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
        if (minPrice != null || maxPrice != null || location != null || status != null || attrKey != null) {
            properties = propertyService.searchProperties(
                    minPrice, maxPrice, location, category, status, attrKey, attrValue, pageable);
        } else {
            properties = propertyService.getAllProperties(category, pageable);
        }

        return ResponseEntity.ok(properties);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new property", description = "Admins can create new properties with dynamic attributes")
    public ResponseEntity<PropertyDTO> createProperty(@Valid @RequestBody PropertyDTO propertyDTO) {
        return new ResponseEntity<>(propertyService.createProperty(propertyDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a property", description = "Remove a property listing from the store")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update property details", description = "Modify an existing property and its dynamic attributes")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id,
            @Valid @RequestBody PropertyDTO propertyDTO) {
        return ResponseEntity.ok(propertyService.updateProperty(id, propertyDTO));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Upload property image", description = "Upload a file and add its URL to the property's image set")
    public ResponseEntity<PropertyDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        PropertyDTO property = propertyService.getPropertyById(id);
        String tenantId = TenantContext.getCurrentTenant();

        String imageUrl = fileUploadService.storeFile(file, tenantId);

        if (property.getImageUrls() == null) {
            property.setImageUrls(new java.util.ArrayList<>());
        }
        property.getImageUrls().add(imageUrl);

        return ResponseEntity.ok(propertyService.updateProperty(id, property));
    }
}
