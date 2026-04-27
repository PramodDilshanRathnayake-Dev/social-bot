package com.socialbot.social_bot.service.whatsapp;

public interface WhatsAppSender {
    void sendTextMessage(String to, String messageBody);
}

