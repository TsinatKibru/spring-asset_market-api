package com.assetmarket.api.dto;

import com.assetmarket.api.entity.ViewingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingRequestDTO {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long userId;
    private String username;
    private LocalDateTime requestedAt;
    private ViewingStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
