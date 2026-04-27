package com.socialbot.social_bot.service.faq;

import com.socialbot.social_bot.repository.FaqEntryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaFaqService implements FaqService {

    private final FaqEntryRepository faqEntryRepository;

    public JpaFaqService(FaqEntryRepository faqEntryRepository) {
        this.faqEntryRepository = faqEntryRepository;
    }

    @Override
    public Optional<String> findAnswer(String normalizedQuestionKey) {
        if (normalizedQuestionKey == null || normalizedQuestionKey.isBlank()) return Optional.empty();
        return faqEntryRepository
                .findFirstByQuestionKeyAndEnabledTrueOrderByUpdatedAtDesc(normalizedQuestionKey)
                .map(e -> e.getAnswerBody());
    }
}

