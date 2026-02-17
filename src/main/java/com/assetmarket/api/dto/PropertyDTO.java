package com.assetmarket.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PropertyDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Location is required")
    @Schema(description = "Property location", example = "123 Main St, Springfield")
    private String location;

    @Schema(description = "Name of the existing category this property belongs to", example = "Residential")
    private String categoryName;

    @Schema(description = "Current status of the property", example = "AVAILABLE")
    private com.assetmarket.api.entity.PropertyStatus status;

    private java.util.List<String> imageUrls;

    @Schema(description = "Dynamic attributes based on the category's schema", example = "{\"bedrooms\": 3, \"hasGarage\": true}")
    private Map<String, Object> attributes;
}
