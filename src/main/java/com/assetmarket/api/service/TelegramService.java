package com.assetmarket.api.service;

import com.assetmarket.api.entity.User;
import com.assetmarket.api.repository.UserRepository;
import com.assetmarket.api.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    /**
     * Authenticates a user based on Telegram initData.
     * Finds existing user by telegramId or creates a new one.
     */
    public String authenticate(String initData, String tenantId) {
        if (!validateInitData(initData)) {
            return null;
        }

        Map<String, String> queries = parseQueryString(initData);
        String userJson = queries.get("user");

        // Extract ID from JSON (simplified extraction for now)
        String telegramId = userJson.replaceAll(".*\"id\":(\\d+),.*", "$1");

        User user = userRepository.findByTelegramIdAndTenantId(telegramId, tenantId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username("tg_" + telegramId)
                            .telegramId(telegramId)
                            .email(telegramId + "@telegram.com")
                            .password(passwordEncoder.encode(telegramId))
                            .roles(new java.util.HashSet<>(
                                    java.util.Arrays.asList(com.assetmarket.api.entity.Role.ROLE_USER)))
                            .tenantId(tenantId)
                            .build();
                    return userRepository.save(newUser);
                });

        return jwtUtils.generateTokenFromUsername(user.getUsername());
    }

    private Map<String, String> parseQueryString(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        p -> p[0],
                        p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8),
                        (oldValue, newValue) -> oldValue));
    }

    /**
     * Validates the data sent from the Telegram Mini App (initData).
     * 
     * @param initData The raw initData string from the frontend.
     * @return true if valid, false otherwise.
     */
    public boolean validateInitData(String initData) {
        try {
            Map<String, String> queries = parseQueryString(initData);
            String hash = queries.get("hash");
            queries.remove("hash");

            String dataCheckString = queries.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));

            byte[] secretKey = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, "WebAppData")
                    .hmac(botToken);
            String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey)
                    .hmacHex(dataCheckString);

            return hmac.equalsIgnoreCase(hash);
        } catch (Exception e) {
            log.error("Failed to validate Telegram initData", e);
            return false;
        }
    }

    /**
     * Sends a simple text message via the Telegram Bot.
     */
    public void sendBotMessage(String telegramId, String text) {
        if (telegramId == null || telegramId.isEmpty())
            return;

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        Map<String, Object> body = Map.of(
                "chat_id", telegramId,
                "text", text,
                "parse_mode", "Markdown");

        try {
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram message to {}", telegramId, e);
        }
    }

    /**
     * Sends a message with an inline "Open App" button.
     */
    public void sendBotMessageWithButton(String telegramId, String text, String buttonText, String appUrl) {
        if (telegramId == null || telegramId.isEmpty())
            return;

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        Map<String, Object> webApp = Map.of("url", appUrl);
        Map<String, Object> button = Map.of("text", buttonText, "web_app", webApp);
        Map<String, Object> replyMarkup = Map.of("inline_keyboard", Arrays.asList(Arrays.asList(button)));

        Map<String, Object> body = Map.of(
                "chat_id", telegramId,
                "text", text,
                "parse_mode", "Markdown",
                "reply_markup", replyMarkup);

        try {
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram message with button to {}", telegramId, e);
        }
    }

    /**
     * Processes incoming updates from the Telegram Webhook.
     */
    @SuppressWarnings("unchecked")
    public void handleUpdate(Map<String, Object> update) {
        if (!update.containsKey("message"))
            return;

        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (!message.containsKey("text") || !message.containsKey("from"))
            return;

        String text = (String) message.get("text");
        Map<String, Object> from = (Map<String, Object>) message.get("from");
        String telegramId = String.valueOf(from.get("id"));

        log.debug("Processing message from {}: {}", telegramId, text);

        if (text.startsWith("/start")) {
            String[] parts = text.split(" ");
            String tenantId = parts.length > 1 ? parts[1] : "default";

            String welcomeMsg = String.format("🚀 *Welcome to Asset Marketplace!* \n\n" +
                    "You are accessing the *%s* collection. Browse our premium listings below.",
                    tenantId.toUpperCase());

            // Generate link for the Mini App with the tenant ID in the URL
            String appBaseUrl = "https://optimally-unmunitioned-raeann.ngrok-free.dev";
            String appUrl = appBaseUrl + "?tenant=" + tenantId;

            sendBotMessageWithButton(telegramId, welcomeMsg, "💎 Browse Market", appUrl);
        }
    }
}
