package com.assetmarket.api.controller;

import com.assetmarket.api.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {

    private final TelegramService telegramService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleUpdate(@RequestBody Map<String, Object> update) {
        log.info("Received Telegram update: {}", update);
        try {
            telegramService.handleUpdate(update);
        } catch (Exception e) {
            log.error("Error processing Telegram update", e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> authRequest,
            @RequestHeader(value = "X-Tenant-ID", required = true) String tenantId) {
        String initData = authRequest.get("initData");
        String jwt = telegramService.authenticate(initData, tenantId);
        if (jwt != null) {
            return ResponseEntity.ok(Map.of("accessToken", jwt, "tokenType", "Bearer"));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid Telegram data"));
    }
}
