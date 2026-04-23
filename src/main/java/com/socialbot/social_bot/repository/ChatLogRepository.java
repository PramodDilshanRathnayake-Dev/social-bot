package com.socialbot.social_bot.repository;

import com.socialbot.social_bot.model.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findByUserPhoneOrderByTimestampAsc(String userPhone);
}
