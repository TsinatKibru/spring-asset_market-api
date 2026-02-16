package com.assetmarket.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    // Tenant Info
    @NotBlank
    @Size(min = 3, max = 50)
    private String companyName;

    @NotBlank
    @Size(min = 3, max = 20)
    private String slug;

    // Admin Info
    @NotBlank
    @Size(min = 3, max = 20)
    private String adminUsername;

    @NotBlank
    @Size(max = 50)
    @Email
    private String adminEmail;

    @NotBlank
    @Size(min = 6, max = 40)
    private String adminPassword;
}
