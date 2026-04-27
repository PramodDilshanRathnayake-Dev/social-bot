package com.socialbot.social_bot.repository;

import com.socialbot.social_bot.model.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaqEntryRepository extends JpaRepository<FaqEntry, Long> {
    Optional<FaqEntry> findFirstByQuestionKeyAndEnabledTrueOrderByUpdatedAtDesc(String questionKey);
}

