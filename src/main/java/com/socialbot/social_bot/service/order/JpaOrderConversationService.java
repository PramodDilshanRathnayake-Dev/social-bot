package com.socialbot.social_bot.service.order;

import com.socialbot.social_bot.model.ClientOrder;
import com.socialbot.social_bot.repository.ClientOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class JpaOrderConversationService implements OrderConversationService {

    private static final List<ClientOrder.Status> ACTIVE_STATUSES =
            List.of(ClientOrder.Status.DRAFT, ClientOrder.Status.AWAITING_CONFIRMATION);

    private static final Pattern PHONE = Pattern.compile("^\\+?\\d{7,15}$");

    private final ClientOrderRepository orderRepository;
    private final PackageCatalog packageCatalog;
    private final AdminNotifier adminNotifier;

    public JpaOrderConversationService(
            ClientOrderRepository orderRepository,
            PackageCatalog packageCatalog,
            AdminNotifier adminNotifier
    ) {
        this.orderRepository = orderRepository;
        this.packageCatalog = packageCatalog;
        this.adminNotifier = adminNotifier;
    }

    @Override
    public Optional<String> maybeHandle(String from, String messageBody) {
        String msg = (messageBody == null) ? "" : messageBody.trim();
        if (from == null || from.isBlank()) return Optional.empty();

        ClientOrder order = orderRepository
                .findTopByProspectPhoneAndStatusInOrderByUpdatedAtDesc(from, ACTIVE_STATUSES)
                .orElse(null);

        boolean start = isStartOrder(msg);
        if (order == null && !start) return Optional.empty();
        if (order == null) {
            order = orderRepository.save(new ClientOrder(from));
        }

        if (isCancel(msg)) {
            order.setStatus(ClientOrder.Status.CANCELLED);
            orderRepository.save(order);
            return Optional.of("Order cancelled. If you want to start again, reply: ORDER");
        }

        switch (order.getStep()) {
            case SELECT_PACKAGE -> {
                return Optional.of(handleSelectPackage(order, msg));
            }
            case ASK_CLIENT_BUSINESS_NUMBER -> {
                return Optional.of(handleClientBusinessNumber(order, msg));
            }
            case ASK_CLIENT_PERSONAL_NOTIFY_NUMBER -> {
                return Optional.of(handleClientPersonalNotifyNumber(order, msg));
            }
            case CONFIRM -> {
                return Optional.of(handleConfirm(order, msg));
            }
        }

        return Optional.empty();
    }

    private String handleSelectPackage(ClientOrder order, String msg) {
        var packages = packageCatalog.list();
        if (packages.isEmpty()) {
            order.setStatus(ClientOrder.Status.CANCELLED);
            orderRepository.save(order);
            return "Packages are not configured right now. Please contact us to place an order.";
        }

        if (msg.isBlank() || isStartOrder(msg) || "packages".equalsIgnoreCase(msg)) {
            return renderPackageList(packages);
        }

        var chosen = packageCatalog.findByCode(msg);
        if (chosen.isEmpty()) {
            return "Please reply with a package code.\n\n" + renderPackageList(packages);
        }

        var tier = chosen.get();
        order.setPackageCode(tier.getCode());
        order.setPackageName(tier.getName());
        order.setPackagePriceMinor(tier.getPriceMinor());
        order.setCurrency(tier.getCurrency());
        order.setStep(ClientOrder.Step.ASK_CLIENT_BUSINESS_NUMBER);
        orderRepository.save(order);

        return "Please send the client's WhatsApp Business number (E.164, e.g. +94XXXXXXXXX).";
    }

    private String renderPackageList(List<com.socialbot.social_bot.config.WhatzziProperties.PackageTier> packages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Please choose a package by replying with the code:\n");
        for (var p : packages) {
            sb.append("- ").append(safe(p.getCode()))
                    .append(": ").append(safe(p.getName()))
                    .append(" (").append(safe(p.getCurrency())).append(" ").append(p.getPriceMinor()).append(")");
            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                sb.append(" - ").append(p.getDescription().trim());
            }
            sb.append("\n");
        }
        sb.append("\nReply CANCEL anytime to cancel.");
        return sb.toString();
    }

    private String handleClientBusinessNumber(ClientOrder order, String msg) {
        if (!isPhone(msg)) {
            return "Invalid number format. Please send the client's WhatsApp Business number in E.164 format (e.g. +94XXXXXXXXX).";
        }

        order.setClientWhatsappBusinessNumber(normalizePhone(msg));
        order.setStep(ClientOrder.Step.ASK_CLIENT_PERSONAL_NOTIFY_NUMBER);
        orderRepository.save(order);

        return "Please send the client's personal WhatsApp number to receive order notifications (E.164).";
    }

    private String handleClientPersonalNotifyNumber(ClientOrder order, String msg) {
        if (!isPhone(msg)) {
            return "Invalid number format. Please send the client's personal WhatsApp number in E.164 format (e.g. +94XXXXXXXXX).";
        }

        order.setClientPersonalNotifyNumber(normalizePhone(msg));
        order.setStep(ClientOrder.Step.CONFIRM);
        order.setStatus(ClientOrder.Status.AWAITING_CONFIRMATION);
        orderRepository.save(order);

        return renderSummary(order);
    }

    private String handleConfirm(ClientOrder order, String msg) {
        String normalized = (msg == null) ? "" : msg.trim().toLowerCase(Locale.ROOT);
        if ("confirm".equals(normalized)) {
            order.setStatus(ClientOrder.Status.SUBMITTED);
            order.setSubmittedAt(LocalDateTime.now());
            orderRepository.save(order);
            adminNotifier.notifySubmittedOrder(order);
            return "Thank you. Your order is submitted. We will contact you soon.";
        }

        return "Please reply CONFIRM to submit the order, or CANCEL to cancel.\n\n" + renderSummary(order);
    }

    private String renderSummary(ClientOrder o) {
        return "ORDER SUMMARY\n"
                + "Package: " + safe(o.getPackageCode()) + " - " + safe(o.getPackageName()) + "\n"
                + "Price: " + safe(o.getCurrency()) + " " + (o.getPackagePriceMinor() == null ? "" : o.getPackagePriceMinor()) + "\n"
                + "Client WA Business: " + safe(o.getClientWhatsappBusinessNumber()) + "\n"
                + "Client Personal Notify: " + safe(o.getClientPersonalNotifyNumber()) + "\n\n"
                + "Reply CONFIRM to submit, or CANCEL to cancel.";
    }

    private static boolean isStartOrder(String msg) {
        if (msg == null) return false;
        String n = msg.trim().toLowerCase(Locale.ROOT);
        return "order".equals(n) || "place order".equals(n);
    }

    private static boolean isCancel(String msg) {
        if (msg == null) return false;
        String n = msg.trim().toLowerCase(Locale.ROOT);
        return "cancel".equals(n) || "stop".equals(n);
    }

    private static boolean isPhone(String msg) {
        if (msg == null) return false;
        return PHONE.matcher(msg.trim()).matches();
    }

    private static String normalizePhone(String msg) {
        return msg.trim();
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}

