package com.socialbot.social_bot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_user_phone", nullable = false)
    private String userPhone;

    @Column(name = "message_direction", nullable = false)
    private String direction; // "INCOMING" or "OUTGOING"

    @Column(name = "message_body", length = 2000)
    private String messageBody;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public ChatLog() {}

    public ChatLog(String userPhone, String direction, String messageBody, LocalDateTime timestamp) {
        this.userPhone = userPhone;
        this.direction = direction;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getUserPhone() { return userPhone; }
    public String getDirection() { return direction; }
    public String getMessageBody() { return messageBody; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
