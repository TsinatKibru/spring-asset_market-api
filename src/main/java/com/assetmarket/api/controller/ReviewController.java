package com.assetmarket.api.controller;

import com.assetmarket.api.dto.ReviewDTO;
import com.assetmarket.api.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews & Ratings", description = "Endpoints for sharing property feedback")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Leave a review", description = "Requires prior verified interaction (inquiry or viewing) with the property")
    public ResponseEntity<ReviewDTO> createReview(@RequestBody Map<String, Object> request) {
        Long propertyId = Long.valueOf(request.get("propertyId").toString());
        Integer rating = (Integer) request.get("rating");
        String comment = request.get("comment") != null ? request.get("comment").toString() : null;

        return ResponseEntity.ok(reviewService.createReview(propertyId, rating, comment));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get property reviews", description = "List all reviews for a specific property")
    public ResponseEntity<Page<ReviewDTO>> getReviewsForProperty(
            @PathVariable Long propertyId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsForProperty(propertyId, pageable));
    }
}
