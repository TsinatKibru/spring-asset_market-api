package com.assetmarket.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeSchemaDTO {
    @Schema(description = "Name of the attribute", example = "bedrooms")
    private String name;

    @Schema(description = "Type of the attribute", example = "number", allowableValues = { "string", "number",
            "boolean" })
    private String type; // e.g., "number", "string", "boolean"

    @Schema(description = "Whether the attribute is mandatory", example = "true")
    private boolean required;
}
