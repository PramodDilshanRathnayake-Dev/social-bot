package com.socialbot.social_bot.service.faq;

import java.util.Optional;

public interface FaqService {
    Optional<String> findAnswer(String normalizedQuestionKey);
}

