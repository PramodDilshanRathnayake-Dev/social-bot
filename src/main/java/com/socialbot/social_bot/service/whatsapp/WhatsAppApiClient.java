package com.socialbot.social_bot.service.whatsapp;

import com.socialbot.social_bot.config.WhatsAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WhatsAppApiClient implements WhatsAppSender {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppApiClient.class);

    private final WhatsAppConfig config;
    private final RestClient restClient;

    public WhatsAppApiClient(WhatsAppConfig config) {
        this.config = config;
        this.restClient = RestClient.builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader("Authorization", "Bearer " + config.getAccessToken())
                .build();
    }

    @Override
    public void sendTextMessage(String to, String messageBody) {
        String normalizedTo = normalizeTo(to);
        String url = config.getApiUrl() + String.format("/%s/messages", config.getPhoneNumberId());

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", normalizedTo,
                "type", "text",
                "text", Map.of("preview_url", false, "body", messageBody)
        );

        logger.info("Sending WhatsApp message to {}", normalizedTo);
        restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private static String normalizeTo(String to) {
        if (to == null) return "";
        String s = to.trim().replace(" ", "");
        if (s.startsWith("+")) {
            return s.substring(1);
        }
        return s;
    }
}
