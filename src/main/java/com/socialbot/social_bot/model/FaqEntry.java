package com.socialbot.social_bot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "faq_entries",
        indexes = {
                @Index(name = "idx_faq_entries_question_key", columnList = "question_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_faq_key_lang", columnNames = {"question_key", "language_code"})
        }
)
public class FaqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_key", nullable = false, length = 500)
    private String questionKey;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "answer_body", nullable = false, length = 4000)
    private String answerBody;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FaqEntry() {}

    public FaqEntry(String questionKey, String languageCode, String answerBody, boolean enabled) {
        this.questionKey = questionKey;
        this.languageCode = languageCode;
        this.answerBody = answerBody;
        this.enabled = enabled;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getQuestionKey() { return questionKey; }
    public String getLanguageCode() { return languageCode; }
    public String getAnswerBody() { return answerBody; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

