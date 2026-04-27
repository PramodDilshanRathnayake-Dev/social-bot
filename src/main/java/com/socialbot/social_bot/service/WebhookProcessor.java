package com.socialbot.social_bot.service;

import java.util.Map;

public interface WebhookProcessor {
    void processIncomingWebhook(Map<String, Object> payload);
}

