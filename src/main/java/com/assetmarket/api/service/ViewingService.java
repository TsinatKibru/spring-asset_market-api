package com.assetmarket.api.service;

import com.assetmarket.api.dto.ViewingRequestDTO;
import com.assetmarket.api.entity.*;
import com.assetmarket.api.repository.MessageRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.repository.ViewingRequestRepository;
import com.assetmarket.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ViewingService {

    private final ViewingRequestRepository viewingRequestRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public ViewingRequestDTO requestViewing(Long propertyId, LocalDateTime dateTime, String notes) {
        User user = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        ViewingRequest request = ViewingRequest.builder()
                .property(property)
                .user(user)
                .requestedAt(dateTime)
                .status(ViewingStatus.PENDING)
                .notes(notes)
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        request = viewingRequestRepository.save(request);

        // System message in chat
        String messageContent = String.format("📅 [SYSTEM] Viewing request for %s. Notes: %s",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                notes != null ? notes : "None");

        messageRepository.save(Message.builder()
                .property(property)
                .sender(user)
                .content(messageContent)
                .tenantId(TenantContext.getCurrentTenant())
                .build());

        return convertToDTO(request);
    }

    @Transactional
    public ViewingRequestDTO updateStatus(Long requestId, ViewingStatus status) {
        User requester = getCurrentUser();
        ViewingRequest request = viewingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing request not found"));

        boolean isAdmin = requester.getRoles().contains(Role.ROLE_ADMIN);

        // Validation: Only admin can approve/reject. User can only cancel.
        if (status == ViewingStatus.APPROVED || status == ViewingStatus.REJECTED) {
            if (!isAdmin) {
                throw new IllegalArgumentException("Unauthorized to approve/reject viewings");
            }
        } else if (status == ViewingStatus.CANCELLED) {
            if (!isAdmin && !request.getUser().getId().equals(requester.getId())) {
                throw new IllegalArgumentException("Unauthorized to cancel this viewing");
            }
        }

        request.setStatus(status);
        request = viewingRequestRepository.save(request);

        // Notify chat
        String messageContent = String.format("📅 [SYSTEM] Viewing request status updated to: %s", status);
        messageRepository.save(Message.builder()
                .property(request.getProperty())
                .sender(requester)
                .content(messageContent)
                .tenantId(TenantContext.getCurrentTenant())
                .build());

        return convertToDTO(request);
    }

    @Transactional(readOnly = true)
    public Page<ViewingRequestDTO> getMyViewings(Pageable pageable) {
        User user = getCurrentUser();
        return viewingRequestRepository.findByUserAndTenantId(user, TenantContext.getCurrentTenant(), pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ViewingRequestDTO> getMerchantViewings(Pageable pageable) {
        return viewingRequestRepository.findByTenantId(TenantContext.getCurrentTenant(), pageable)
                .map(this::convertToDTO);
    }

    private ViewingRequestDTO convertToDTO(ViewingRequest request) {
        return ViewingRequestDTO.builder()
                .id(request.getId())
                .propertyId(request.getProperty().getId())
                .propertyTitle(request.getProperty().getTitle())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .requestedAt(request.getRequestedAt())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdAt(request.getCreatedAt())
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
