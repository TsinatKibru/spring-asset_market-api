package com.assetmarket.api.service;

import com.assetmarket.api.dto.ReviewDTO;
import com.assetmarket.api.entity.*;
import com.assetmarket.api.repository.*;
import com.assetmarket.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ViewingRequestRepository viewingRequestRepository;

    @Transactional
    public ReviewDTO createReview(Long propertyId, Integer rating, String comment) {
        User user = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        // 1. One review per property
        if (reviewRepository.existsByPropertyIdAndUserId(propertyId, user.getId())) {
            throw new IllegalArgumentException("You have already reviewed this property");
        }

        // 2. Verified Interaction Check
        boolean hasInquiry = messageRepository.existsByPropertyAndSender(property, user);
        boolean hasViewing = viewingRequestRepository
                .findByUserAndTenantId(user, property.getTenantId(), Pageable.unpaged())
                .stream().anyMatch(vr -> vr.getProperty().getId().equals(propertyId));

        if (!hasInquiry && !hasViewing) {
            throw new IllegalArgumentException(
                    "Only verified users (with prior inquiries or viewings) can leave a review");
        }

        Review review = Review.builder()
                .property(property)
                .user(user)
                .rating(rating)
                .comment(comment)
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        return convertToDTO(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsForProperty(Long propertyId, Pageable pageable) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        return reviewRepository.findByPropertyAndTenantId(property, TenantContext.getCurrentTenant(), pageable)
                .map(this::convertToDTO);
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .propertyId(review.getProperty().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsernameAndTenantId(username, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalStateException("User context not found"));
    }
}
