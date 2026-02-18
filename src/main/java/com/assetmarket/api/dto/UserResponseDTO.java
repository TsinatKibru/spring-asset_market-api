package com.assetmarket.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private String tenantId;
}
