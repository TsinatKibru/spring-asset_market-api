package com.assetmarket.api.service;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.PropertyStatus;
import com.assetmarket.api.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@lombok.extern.slf4j.Slf4j
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.assetmarket.api.repository.ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<PropertyDTO> getAllProperties(String category, Pageable pageable) {
        Page<Property> properties;
        if (category != null && !category.isEmpty()) {
            properties = propertyRepository.findByCategoryName(category, pageable);
        } else {
            properties = propertyRepository.findAll(pageable);
        }
        return properties.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PropertyDTO> searchProperties(
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            String location,
            String categoryName,
            PropertyStatus status,
            java.util.Map<String, String> attributes,
            Pageable pageable) {

        Long categoryId = null;
        Category category = null;
        if (categoryName != null && !categoryName.isEmpty()) {
            category = categoryRepository.findByName(categoryName).orElse(null);
            if (category != null) {
                categoryId = category.getId();
            } else {
                log.warn("Search attempted with non-existent category: {}", categoryName);
                // We could throw here, but let's just proceed without category filter
            }
        }

        String statusStr = status != null ? status.name() : null;
        String tenantId = TenantContext.getCurrentTenant();

        // Translate sort properties for native query
        org.springframework.data.domain.Sort translatedSort = org.springframework.data.domain.Sort.by(
                pageable.getSort().stream()
                        .map(order -> {
                            String property = order.getProperty();
                            if (property.equals("createdAt")) {
                                return new org.springframework.data.domain.Sort.Order(order.getDirection(),
                                        "created_at");
                            }
                            if (property.equals("updatedAt")) {
                                return new org.springframework.data.domain.Sort.Order(order.getDirection(),
                                        "updated_at");
                            }
                            return order;
                        })
                        .toList());

        Pageable nativePageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                translatedSort);

        String attributesJson = null;
        if (attributes != null && !attributes.isEmpty()) {
            try {
                java.util.Map<String, Object> typedAttributes = new java.util.HashMap<>();
                if (category != null && category.getAttributeSchema() != null) {
                    for (java.util.Map.Entry<String, String> entry : attributes.entrySet()) {
                        String key = entry.getKey();
                        String val = entry.getValue();

                        // Find attribute definition in schema
                        java.util.Optional<java.util.Map<String, Object>> attrSchema = category.getAttributeSchema()
                                .stream()
                                .filter(s -> key.equals(s.get("name")))
                                .findFirst();

                        if (attrSchema.isPresent()) {
                            String type = (String) attrSchema.get().get("type");
                            if ("number".equals(type)) {
                                try {
                                    if (val.contains(".")) {
                                        typedAttributes.put(key, Double.parseDouble(val));
                                    } else {
                                        typedAttributes.put(key, Long.parseLong(val));
                                    }
                                } catch (NumberFormatException e) {
                                    typedAttributes.put(key, val);
                                }
                            } else if ("boolean".equals(type)) {
                                typedAttributes.put(key, Boolean.parseBoolean(val));
                            } else {
                                typedAttributes.put(key, val);
                            }
                        } else {
                            typedAttributes.put(key, val);
                        }
                    }
                } else {
                    typedAttributes.putAll(attributes);
                }
                attributesJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(typedAttributes);
            } catch (Exception e) {
                log.error("Failed to serialize attributes for filtering", e);
            }
        }

        return propertyRepository.findWithFilters(
                minPrice, maxPrice, location, categoryId, statusStr, attributesJson, tenantId, nativePageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public PropertyDTO createProperty(PropertyDTO propertyDTO) {
        Category category = categoryRepository.findByName(propertyDTO.getCategoryName())
                .orElseThrow(
                        () -> new IllegalArgumentException("Category not found: " + propertyDTO.getCategoryName()));

        Property property = Property.builder()
                .title(propertyDTO.getTitle())
                .description(propertyDTO.getDescription())
                .price(propertyDTO.getPrice())
                .location(propertyDTO.getLocation())
                .category(category)
                .status(propertyDTO.getStatus() != null ? propertyDTO.getStatus() : PropertyStatus.AVAILABLE)
                .attributes(propertyDTO.getAttributes())
                .imageUrls(
                        propertyDTO.getImageUrls() != null ? propertyDTO.getImageUrls() : new java.util.ArrayList<>())
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        // Validation logic
        if (category.getAttributeSchema() == null || category.getAttributeSchema().isEmpty()) {
            throw new IllegalArgumentException("Category '" + category.getName()
                    + "' has no validation schema defined. Please update the category first.");
        }

        validateAttributes(propertyDTO.getAttributes(), category.getAttributeSchema());

        Property savedProperty = propertyRepository.save(property);
        return convertToDTO(savedProperty);
    }

    private void validateAttributes(java.util.Map<String, Object> attributes,
            java.util.List<java.util.Map<String, Object>> schema) {
        if (schema == null || schema.isEmpty())
            return;
        if (attributes == null)
            attributes = new java.util.HashMap<>();

        for (java.util.Map<String, Object> fieldSchema : schema) {
            String name = (String) fieldSchema.get("name");
            String type = (String) fieldSchema.get("type");
            boolean required = fieldSchema.get("required") != null && (boolean) fieldSchema.get("required");

            Object value = attributes.get(name);

            if (required && value == null) {
                throw new IllegalArgumentException("Metadata field '" + name + "' is required for this category.");
            }

            if (value != null) {
                validateType(name, value, type);
            }
        }

        // Strict Check: No unknown fields
        for (String key : attributes.keySet()) {
            boolean known = schema.stream().anyMatch(f -> f.get("name").equals(key));
            if (!known) {
                throw new IllegalArgumentException("Metadata field '" + key + "' is not recognized for this category.");
            }
        }
    }

    private void validateType(String name, Object value, String expectedType) {
        switch (expectedType.toLowerCase()) {
            case "number" -> {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException("Field '" + name + "' must be a number.");
                }
            }
            case "boolean" -> {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Field '" + name + "' must be a boolean.");
                }
            }
            case "string" -> {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("Field '" + name + "' must be a string.");
                }
            }
        }
    }

    public PropertyDTO convertToDTO(Property property) {
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setPrice(property.getPrice());
        dto.setLocation(property.getLocation());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());
        dto.setStatus(property.getStatus());
        dto.setImageUrls(property.getImageUrls());
        dto.setCategoryName(property.getCategory() != null ? property.getCategory().getName() : null);
        dto.setAttributes(property.getAttributes());

        // Attach ratings
        dto.setAverageRating(reviewRepository.getAverageRatingForProperty(property.getId()));
        dto.setReviewCount(reviewRepository.countByPropertyId(property.getId()));

        return dto;
    }

    @Transactional(readOnly = true)
    public PropertyDTO getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        return convertToDTO(property);
    }

    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // Tenant check
        if (!property.getTenantId().equals(com.assetmarket.api.security.TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        propertyRepository.delete(property);
    }

    @Transactional
    public PropertyDTO updateProperty(Long id, PropertyDTO propertyDTO) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        // Tenant check
        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        // Update basic fields
        property.setTitle(propertyDTO.getTitle());
        property.setDescription(propertyDTO.getDescription());
        property.setPrice(propertyDTO.getPrice());
        property.setLocation(propertyDTO.getLocation());
        if (propertyDTO.getStatus() != null) {
            property.setStatus(propertyDTO.getStatus());
        }

        if (propertyDTO.getImageUrls() != null) {
            property.setImageUrls(new java.util.ArrayList<>(propertyDTO.getImageUrls()));
        }

        // Handle Category Change (if provided and different)
        Category category = property.getCategory();
        if (propertyDTO.getCategoryName() != null && !propertyDTO.getCategoryName().equals(category.getName())) {
            category = categoryRepository.findByName(propertyDTO.getCategoryName())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Category not found: " + propertyDTO.getCategoryName()));
            property.setCategory(category);
        }

        // Validation logic for attributes
        if (category.getAttributeSchema() == null || category.getAttributeSchema().isEmpty()) {
            throw new IllegalArgumentException("Category '" + category.getName()
                    + "' has no validation schema defined. Please update the category first.");
        }

        validateAttributes(propertyDTO.getAttributes(), category.getAttributeSchema());
        property.setAttributes(propertyDTO.getAttributes());

        Property savedProperty = propertyRepository.save(property);
        return convertToDTO(savedProperty);
    }
}
