package com.socialbot.social_bot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_prospect_phone_created_at", columnList = "prospect_phone,created_at"),
                @Index(name = "idx_orders_status_created_at", columnList = "status,created_at")
        }
)
public class ClientOrder {

    public enum Status {
        DRAFT,
        AWAITING_CONFIRMATION,
        SUBMITTED,
        CANCELLED
    }

    public enum Step {
        SELECT_PACKAGE,
        ASK_CLIENT_BUSINESS_NUMBER,
        ASK_CLIENT_PERSONAL_NOTIFY_NUMBER,
        CONFIRM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false, length = 80)
    private Step step = Step.SELECT_PACKAGE;

    @Column(name = "prospect_phone", nullable = false, length = 50)
    private String prospectPhone;

    @Column(name = "package_code", length = 100)
    private String packageCode;

    @Column(name = "package_name", length = 200)
    private String packageName;

    @Column(name = "package_price_minor")
    private Long packagePriceMinor;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "client_whatsapp_business_number", length = 50)
    private String clientWhatsappBusinessNumber;

    @Column(name = "client_personal_notify_number", length = 50)
    private String clientPersonalNotifyNumber;

    @Column(name = "language_code", length = 10)
    private String languageCode = "en";

    @Column(name = "notes", length = 4000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    protected ClientOrder() {}

    public ClientOrder(String prospectPhone) {
        this.prospectPhone = prospectPhone;
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
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Step getStep() { return step; }
    public void setStep(Step step) { this.step = step; }
    public String getProspectPhone() { return prospectPhone; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public Long getPackagePriceMinor() { return packagePriceMinor; }
    public void setPackagePriceMinor(Long packagePriceMinor) { this.packagePriceMinor = packagePriceMinor; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getClientWhatsappBusinessNumber() { return clientWhatsappBusinessNumber; }
    public void setClientWhatsappBusinessNumber(String clientWhatsappBusinessNumber) { this.clientWhatsappBusinessNumber = clientWhatsappBusinessNumber; }
    public String getClientPersonalNotifyNumber() { return clientPersonalNotifyNumber; }
    public void setClientPersonalNotifyNumber(String clientPersonalNotifyNumber) { this.clientPersonalNotifyNumber = clientPersonalNotifyNumber; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}

