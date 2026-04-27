package com.socialbot.social_bot.service;

import com.socialbot.social_bot.repository.ChatLogRepository;
import com.socialbot.social_bot.model.ChatLog;
import com.socialbot.social_bot.service.whatsapp.WhatsAppSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService implements WebhookProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final ChatLogRepository chatLogRepository;
    private final MessageRouter messageRouter;
    private final WhatsAppSender whatsAppSender;

    public WhatsAppService(
            ChatLogRepository chatLogRepository,
            MessageRouter messageRouter,
            WhatsAppSender whatsAppSender
    ) {
        this.chatLogRepository = chatLogRepository;
        this.messageRouter = messageRouter;
        this.whatsAppSender = whatsAppSender;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processIncomingWebhook(Map<String, Object> payload) {
        try {
            // Traverse the messy WhatsApp payload structure
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) return;

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
                if (changes == null || changes.isEmpty()) continue;

                for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    if (value == null || !value.containsKey("messages")) continue;

                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                    if (messages == null || messages.isEmpty()) continue;

                    for (Map<String, Object> message : messages) {
                        String type = (String) message.get("type");
                        String from = (String) message.get("from");
                        
                        if ("text".equals(type)) {
                            Map<String, Object> textObj = (Map<String, Object>) message.get("text");
                            String body = (String) textObj.get("body");
                            logger.info("Inbound WhatsApp text message from {}", from);
                            
                            handleTextMessage(from, body);
                        }
                        // To handle other types like image, audio, etc., add conditions here
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse WhatsApp payload", e);
        }
    }

    private void handleTextMessage(String from, String body) {
        handleTextMessageForApi(from, body, true);
    }

    /**
     * Public wrapper used by API-based testing to exercise the same ChatClient + persistence
     * logic as the WhatsApp webhook flow.
     *
     * @param sendToWhatsapp when true, also calls the Meta WhatsApp send API
     * @return the generated AI response text
     */
    public String handleTextMessageForApi(String from, String body, boolean sendToWhatsapp) {
        // Save incoming message
        chatLogRepository.save(new ChatLog(from, "INCOMING", body, LocalDateTime.now()));

        String reply = messageRouter.route(from, body);

        // Save outgoing message
        chatLogRepository.save(new ChatLog(from, "OUTGOING", reply, LocalDateTime.now()));

        if (sendToWhatsapp) {
            // Send response back via Meta's API
            whatsAppSender.sendTextMessage(from, reply);
        }

        return reply;
    }
}
