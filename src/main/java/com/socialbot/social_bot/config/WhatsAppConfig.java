package com.socialbot.social_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "whatsapp.meta")
public class WhatsAppConfig {

    private String verifyToken;
    private String accessToken;
    private String phoneNumberId;
    private String apiUrl;
    private String appSecret;
    private boolean signatureVerificationEnabled = false;

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public void setPhoneNumberId(String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isSignatureVerificationEnabled() {
        return signatureVerificationEnabled;
    }

    public void setSignatureVerificationEnabled(boolean signatureVerificationEnabled) {
        this.signatureVerificationEnabled = signatureVerificationEnabled;
    }
}
