package com.socialbot.social_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "whatzzi")
public class WhatzziProperties {

    private String adminNumber;
    private String hiReply = "Hello! How can we help you with a WhatsApp chatbot today?";
    private List<PackageTier> packages = new ArrayList<>();

    public String getAdminNumber() {
        return adminNumber;
    }

    public void setAdminNumber(String adminNumber) {
        this.adminNumber = adminNumber;
    }

    public String getHiReply() {
        return hiReply;
    }

    public void setHiReply(String hiReply) {
        this.hiReply = hiReply;
    }

    public List<PackageTier> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageTier> packages) {
        this.packages = packages;
    }

    public static class PackageTier {
        private String code;
        private String name;
        private long priceMinor;
        private String currency;
        private String description;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getPriceMinor() { return priceMinor; }
        public void setPriceMinor(long priceMinor) { this.priceMinor = priceMinor; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

