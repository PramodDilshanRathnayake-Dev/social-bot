package com.socialbot.social_bot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupConfigValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupConfigValidator.class);

    private final WhatsAppConfig whatsAppConfig;
    private final WhatzziProperties whatzziProperties;

    public StartupConfigValidator(WhatsAppConfig whatsAppConfig, WhatzziProperties whatzziProperties) {
        this.whatsAppConfig = whatsAppConfig;
        this.whatzziProperties = whatzziProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (whatsAppConfig.isSignatureVerificationEnabled()) {
            if (whatsAppConfig.getAppSecret() == null || whatsAppConfig.getAppSecret().isBlank()) {
                throw new IllegalStateException("Webhook signature verification is enabled but whatsapp.meta.app-secret is not set");
            }
        }

        if (whatsAppConfig.getAccessToken() == null || whatsAppConfig.getAccessToken().isBlank()) {
            logger.warn("whatsapp.meta.access-token is not set; outbound WhatsApp replies will fail");
        }
        if (whatsAppConfig.getPhoneNumberId() == null || whatsAppConfig.getPhoneNumberId().isBlank()) {
            logger.warn("whatsapp.meta.phone-number-id is not set; outbound WhatsApp replies will fail");
        }

        if (whatzziProperties.getAdminNumber() == null || whatzziProperties.getAdminNumber().isBlank()) {
            logger.warn("whatzzi.admin-number is not set; admin order notifications will be skipped");
        }
        if (whatzziProperties.getPackages() == null || whatzziProperties.getPackages().isEmpty()) {
            logger.warn("whatzzi.packages is empty; ORDER flow will not be able to proceed");
        }
    }
}

