package com.assetmarket.api.controller;

import com.assetmarket.api.dto.MessageDTO;
import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Endpoints for property inquiries and communication")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/inquiry")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Send property inquiry", description = "Sends a message regarding a specific property")
    public ResponseEntity<MessageDTO> sendInquiry(@RequestBody Map<String, Object> request) {
        Long propertyId = Long.valueOf(request.get("propertyId").toString());
        String content = request.get("content").toString();
        return ResponseEntity.ok(messageService.sendInquiry(propertyId, content));
    }

    @GetMapping("/my-inquiries")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List my inquired properties", description = "Get properties I have contacted the merchant about")
    public ResponseEntity<Page<PropertyDTO>> getMyInquiries(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.getMyInquiredProperties(pageable));
    }

    @GetMapping("/thread/{propertyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get conversation thread", description = "Retrieve message history for a property. Admins can specify userId via query param.")
    public ResponseEntity<Page<MessageDTO>> getThread(
            @PathVariable Long propertyId,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(messageService.getThread(propertyId, userId, pageable));
    }
}
