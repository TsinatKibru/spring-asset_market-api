package com.assetmarket.api.controller;

import com.assetmarket.api.dto.ViewingRequestDTO;
import com.assetmarket.api.entity.ViewingStatus;
import com.assetmarket.api.service.ViewingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/viewings")
@RequiredArgsConstructor
@Tag(name = "Viewing Scheduler", description = "Endpoints for property viewing appointments")
public class ViewingController {

    private final ViewingService viewingService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Request a viewing", description = "Submit a requested date and time for a property viewing")
    public ResponseEntity<ViewingRequestDTO> requestViewing(@RequestBody Map<String, Object> request) {
        Long propertyId = Long.valueOf(request.get("propertyId").toString());
        LocalDateTime dateTime = LocalDateTime.parse(request.get("requestedAt").toString());
        String notes = request.get("notes") != null ? request.get("notes").toString() : null;

        return ResponseEntity.ok(viewingService.requestViewing(propertyId, dateTime, notes));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Update viewing status", description = "Admins can approve/reject. Users can cancel.")
    public ResponseEntity<ViewingRequestDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ViewingStatus status) {
        return ResponseEntity.ok(viewingService.updateStatus(id, status));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List my viewing requests", description = "Get status of all your viewing appointments")
    public ResponseEntity<Page<ViewingRequestDTO>> getMyViewings(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(viewingService.getMyViewings(pageable));
    }

    @GetMapping("/merchant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List merchant viewing requests", description = "Admin view of all pending and past viewings for their properties")
    public ResponseEntity<Page<ViewingRequestDTO>> getMerchantViewings(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(viewingService.getMerchantViewings(pageable));
    }
}
