package com.assetmarket.api.service;

import com.assetmarket.api.dto.MessageDTO;
import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Message;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.MessageRepository;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.UserRepository;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyService propertyService;
    private final TelegramService telegramService;

    @Transactional
    public MessageDTO sendInquiry(Long propertyId, String content) {
        User sender = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Property not found in this tenant");
        }

        Message message = Message.builder()
                .property(property)
                .sender(sender)
                .content(content)
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        Message savedMessage = messageRepository.save(message);

        // Notify Sender for confirmation
        if (sender.getTelegramId() != null) {
            telegramService.sendBotMessage(sender.getTelegramId(),
                    "📩 *Inquiry Sent*\nYour message regarding `" + property.getTitle() + "` has been delivered.");
        }

        return convertToDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<PropertyDTO> getMyInquiredProperties(Pageable pageable) {
        User user = getCurrentUser();
        return messageRepository.findInquiredProperties(user.getId(), TenantContext.getCurrentTenant(), pageable)
                .map(propertyService::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getThread(Long propertyId, Long userId, Pageable pageable) {
        // Validation: If requester is a regular user, they can only see their own
        // thread
        User requester = getCurrentUser();
        boolean isAdmin = requester.getRoles().stream().anyMatch(r -> r.name().equals("ROLE_ADMIN"));

        Long targetUserId = userId != null ? userId : requester.getId();

        if (!isAdmin && !targetUserId.equals(requester.getId())) {
            throw new IllegalArgumentException("Unauthorized to view this thread");
        }

        return messageRepository.findThread(propertyId, targetUserId, TenantContext.getCurrentTenant(), pageable)
                .map(this::convertToDTO);
    }

    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .propertyId(message.getProperty().getId())
                .propertyTitle(message.getProperty().getTitle())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
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
