package com.socialbot.social_bot.controller;

import com.socialbot.social_bot.config.WhatsAppConfig;
import com.socialbot.social_bot.security.WebhookSignatureVerifier;
import com.socialbot.social_bot.service.WebhookProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final WhatsAppConfig config;
    private final WebhookProcessor webhookProcessor;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    public WebhookController(
            WhatsAppConfig config,
            WebhookProcessor webhookProcessor,
            WebhookSignatureVerifier signatureVerifier,
            ObjectMapper objectMapper
    ) {
        this.config = config;
        this.webhookProcessor = webhookProcessor;
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
    }

    /**
     * Endpoint for Webhook Verification by Meta.
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && config.getVerifyToken().equals(token)) {
            logger.info("WEBHOOK_VERIFIED");
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Endpoint for receiving incoming messages.
     */
    @PostMapping
    public ResponseEntity<Void> receiveMessage(
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody byte[] rawBody
    ) {
        try {
            if (config.isSignatureVerificationEnabled()) {
                if (!signatureVerifier.verify(rawBody, signature)) {
                    logger.warn("Webhook signature verification failed");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }

            Map<String, Object> payload = parseJson(rawBody);
            webhookProcessor.processIncomingWebhook(payload);
        } catch (Exception e) {
            logger.error("Error processing webhook payload", e);
        }
        
        // Return 200 OK to Meta immediately as required
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> parseJson(byte[] rawBody) {
        if (rawBody == null || rawBody.length == 0) return Collections.emptyMap();
        try {
            return objectMapper.readValue(rawBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse webhook JSON", e);
            return Collections.emptyMap();
        }
    }
}
