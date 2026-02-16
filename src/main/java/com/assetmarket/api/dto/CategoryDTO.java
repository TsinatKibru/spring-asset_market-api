package com.assetmarket.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name is required")
    @Schema(description = "Name of the category", example = "Residential")
    private String name;

    @Schema(description = "Description of the category", example = "Properties for residential living")
    private String description;

    @Schema(description = "List of attributes defining the schema for properties in this category")
    private List<AttributeSchemaDTO> attributeSchema;
}
