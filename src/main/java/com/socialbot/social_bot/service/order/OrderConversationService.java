package com.socialbot.social_bot.service.order;

import java.util.Optional;

public interface OrderConversationService {
    Optional<String> maybeHandle(String from, String messageBody);
}

