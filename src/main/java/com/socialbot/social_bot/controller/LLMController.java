package com.socialbot.social_bot.controller;

import com.socialbot.social_bot.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/llm")
public class LLMController {

    private final WhatsAppService whatsAppService;

    public LLMController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /**
     * Test endpoint to exercise the same ChatClient logic used by the WhatsApp webhook.
     * By default it does NOT call the Meta send-message API.
     */
    @PostMapping("/test-handle-text")
    public ResponseEntity<LlmTestResponse> testHandleText(@RequestBody LlmTestRequest request) {
        String from = (request != null && StringUtils.hasText(request.from())) ? request.from().trim() : "api-test";
        String message = (request != null) ? request.message() : null;
        boolean sendToWhatsapp = request != null && Boolean.TRUE.equals(request.sendToWhatsapp());

        if (!StringUtils.hasText(message)) {
            return ResponseEntity.badRequest().body(new LlmTestResponse(from, "", "", sendToWhatsapp, "message is required"));
        }

        String reply = whatsAppService.handleTextMessageForApi(from, message.trim(), sendToWhatsapp);
        return ResponseEntity.ok(new LlmTestResponse(from, message.trim(), reply, sendToWhatsapp, null));
    }

    public record LlmTestRequest(String from, String message, Boolean sendToWhatsapp) {}

    public record LlmTestResponse(String from, String message, String reply, boolean sendToWhatsapp, String error) {}
}
