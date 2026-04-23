package com.socialbot.social_bot.service;

import com.socialbot.social_bot.config.WhatsAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import com.socialbot.social_bot.repository.ChatLogRepository;
import com.socialbot.social_bot.model.ChatLog;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppConfig config;
    private final ChatClient chatClient;
    private final RestClient restClient;
    private final ChatLogRepository chatLogRepository;

    public WhatsAppService(WhatsAppConfig config, ChatClient.Builder chatClientBuilder, ChatLogRepository chatLogRepository) {
        this.config = config;
        this.chatLogRepository = chatLogRepository;
        
        // Setup ChatClient with basic Memory Advisor to keep discussion context for each user
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a helpful and friendly WhatsApp Chatbot. Keep responses concise and formatted for WhatsApp.")
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
                
        this.restClient = RestClient.builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader("Authorization", "Bearer " + config.getAccessToken())
                .build();
    }

    @SuppressWarnings("unchecked")
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
                            logger.info("Message from {}: {}", from, body);
                            
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
        // Save incoming message
        chatLogRepository.save(new ChatLog(from, "INCOMING", body, LocalDateTime.now()));

        // Use Spring AI to generate a reply
        // We pass the "from" phone number as the chat memory conversationId
        String aiResponse = this.chatClient.prompt()
                .user(body)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, from))
                .system("You are a Business Assistance. Be professional and reject all non-relevant request")
                .call()
                .content();

        // Save outgoing message
        chatLogRepository.save(new ChatLog(from, "OUTGOING", aiResponse, LocalDateTime.now()));

        // Send response back via Meta's API
        sendWhatsAppMessage(from, aiResponse);
    }

    private void sendWhatsAppMessage(String to, String messageBody) {
        String url = config.getApiUrl() + String.format("/%s/messages", config.getPhoneNumberId());

        Map<String, Object> body = Map.of(
            "messaging_product", "whatsapp",
            "recipient_type", "individual",
            "to", to,
            "type", "text",
            "text", Map.of("preview_url", false, "body", messageBody)
        );

        logger.info("Sending reply to {}: {}", to, messageBody);

        restClient.post()
            .uri(url)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }
}
