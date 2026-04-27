package com.socialbot.social_bot.security;

import com.socialbot.social_bot.config.WhatsAppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookSecurityConfiguration {

    @Bean
    WebhookSignatureVerifier webhookSignatureVerifier(WhatsAppConfig config) {
        return new WebhookSignatureVerifier(config.getAppSecret());
    }
}

