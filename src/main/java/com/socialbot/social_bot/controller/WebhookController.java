package com.socialbot.social_bot.controller;

import com.socialbot.social_bot.config.WhatsAppConfig;
import com.socialbot.social_bot.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final WhatsAppConfig config;
    private final WhatsAppService whatsAppService;

    public WebhookController(WhatsAppConfig config, WhatsAppService whatsAppService) {
        this.config = config;
        this.whatsAppService = whatsAppService;
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
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Received WhatsApp Payload: {}", payload);
        
        try {
            whatsAppService.processIncomingWebhook(payload);
        } catch (Exception e) {
            logger.error("Error processing webhook payload", e);
        }
        
        // Return 200 OK to Meta immediately as required
        return ResponseEntity.ok().build();
    }
}
