package com.assetmarket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeSchemaDTO {
    private String name;
    private String type; // e.g., "number", "string", "boolean"
    private boolean required;
}
