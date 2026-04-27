package com.socialbot.social_bot.service.order;

import com.socialbot.social_bot.config.WhatzziProperties;
import com.socialbot.social_bot.model.ClientOrder;
import com.socialbot.social_bot.service.whatsapp.WhatsAppSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminNotifier {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotifier.class);

    private final WhatzziProperties props;
    private final WhatsAppSender whatsAppSender;

    public AdminNotifier(WhatzziProperties props, WhatsAppSender whatsAppSender) {
        this.props = props;
        this.whatsAppSender = whatsAppSender;
    }

    public void notifySubmittedOrder(ClientOrder order) {
        String admin = props.getAdminNumber();
        if (admin == null || admin.isBlank()) {
            logger.error("Admin number is not configured; cannot send order notification");
            return;
        }

        String msg = format(order);
        whatsAppSender.sendTextMessage(admin, msg);
    }

    private static String format(ClientOrder o) {
        return "NEW ORDER SUBMITTED\n"
                + "Prospect: " + safe(o.getProspectPhone()) + "\n"
                + "Package: " + safe(o.getPackageCode()) + " - " + safe(o.getPackageName()) + "\n"
                + "Price: " + safe(o.getCurrency()) + " " + (o.getPackagePriceMinor() == null ? "" : o.getPackagePriceMinor()) + "\n"
                + "Client WA Business: " + safe(o.getClientWhatsappBusinessNumber()) + "\n"
                + "Client Personal Notify: " + safe(o.getClientPersonalNotifyNumber()) + "\n";
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
