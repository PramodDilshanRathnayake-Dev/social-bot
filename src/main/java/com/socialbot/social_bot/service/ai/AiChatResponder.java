package com.socialbot.social_bot.service.ai;

import com.socialbot.social_bot.config.WhatzziProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AiChatResponder implements AiResponder {

    private final ChatClient chatClient;
    private final String systemPrompt;

    public AiChatResponder(ChatClient.Builder chatClientBuilder, WhatzziProperties whatzziProperties) {
        this.systemPrompt = buildSystemPrompt(whatzziProperties);
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    @Override
    public String reply(String conversationId, String userMessage) {
        String content = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .system(systemPrompt)
                .call()
                .content();
        return (content == null) ? "" : content;
    }

    private static String buildSystemPrompt(WhatzziProperties props) {
        String base =
                "You are my marketing assistance. Be professional and market our self. " +
                "Be trustworthy and care about our business safety around fake clients. " +
                "Get a confirmation before submitting an order.\n\n" +
                "Rules:\n" +
                "- Reply in the same language as the user's last message when possible (English, Sinhala, Tamil).\n" +
                "- Do not ask for or share secrets (access tokens, API keys, passwords).\n" +
                "- If the user asks about pricing, only use the configured package tiers below. If none are configured, ask to contact us.\n" +
                "- Keep answers short and business-focused.\n";

        if (props.getPackages() == null || props.getPackages().isEmpty()) {
            return base + "\nConfigured packages: (none)\n";
        }

        String packages = props.getPackages().stream()
                .map(p -> String.format("%s: %s (%s %d) - %s",
                        safe(p.getCode()),
                        safe(p.getName()),
                        safe(p.getCurrency()),
                        p.getPriceMinor(),
                        safe(p.getDescription())))
                .collect(Collectors.joining("\n"));
        return base + "\nConfigured packages:\n" + packages + "\n";
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
