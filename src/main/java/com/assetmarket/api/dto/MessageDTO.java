package com.assetmarket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime createdAt;
}
