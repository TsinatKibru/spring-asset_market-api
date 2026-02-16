package com.assetmarket.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Asset Market API")
                        .version("1.0")
                        .description("### Secure Multi-tenant Property Marketplace API\n\n" +
                                "**Security Requirements:**\n" +
                                "- **Auth Token**: Most endpoints require a `Bearer JWT` token.\n" +
                                "- **Tenant Scoping**: All requests (including Login) require the `X-Tenant-ID` header to scope the context.\n\n"
                                +
                                "**Dynamic Metadata:**\n" +
                                "- Categories define an `attributeSchema` (JSONB).\n" +
                                "- Properties store values in `attributes` (JSONB) matching that schema.\n" +
                                "- Validation is strictly enforced on creation.\n"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addParameters("tenantIdHeader", new Parameter()
                                .in("header")
                                .name("X-Tenant-ID")
                                .description(
                                        "Tenant Identifier (e.g. 'mega-realty').\n\n" +
                                                "**REQUIRED** for:\n" +
                                                "1. Public/Unauthenticated access.\n" +
                                                "2. Login requests.\n\n" +
                                                "**OPTIONAL** if a valid Bearer Token is provided (tenant context is extracted from JWT).")
                                .required(false)
                                .schema(new StringSchema())));
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            boolean headerExists = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> "X-Tenant-ID".equals(p.getName()));

            if (!headerExists) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Tenant-ID")
                        .description("Tenant ID for scoping requests")
                        .required(false)
                        .schema(new StringSchema()));
            }
            return operation;
        };
    }
}
