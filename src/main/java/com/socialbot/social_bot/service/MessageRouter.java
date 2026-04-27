package com.socialbot.social_bot.service;

import com.socialbot.social_bot.service.ai.AiResponder;
import com.socialbot.social_bot.service.faq.FaqService;
import com.socialbot.social_bot.service.greeting.GreetingService;
import com.socialbot.social_bot.service.order.OrderConversationService;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageRouter {

    private final OrderConversationService orderConversationService;
    private final GreetingService greetingService;
    private final FaqService faqService;
    private final AiResponder aiResponder;

    public MessageRouter(
            OrderConversationService orderConversationService,
            GreetingService greetingService,
            FaqService faqService,
            AiResponder aiResponder
    ) {
        this.orderConversationService = orderConversationService;
        this.greetingService = greetingService;
        this.faqService = faqService;
        this.aiResponder = aiResponder;
    }

    public String route(String from, String messageBody) {
        String raw = (messageBody == null) ? "" : messageBody;
        String trimmed = raw.trim();

        // Order flow is deterministic and overrides other routing rules.
        var orderReply = orderConversationService.maybeHandle(from, trimmed);
        if (orderReply.isPresent()) {
            return orderReply.get();
        }

        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (greetingService.isHi(normalized)) {
            return greetingService.reply();
        }

        String faqKey = normalizeFaqKey(trimmed);
        var faqAnswer = faqService.findAnswer(faqKey);
        if (faqAnswer.isPresent()) {
            return faqAnswer.get();
        }

        return aiResponder.reply(from, trimmed);
    }

    static String normalizeFaqKey(String messageBody) {
        if (messageBody == null) return "";
        String s = messageBody.trim().toLowerCase(Locale.ROOT);
        // Collapse internal whitespace to make matching less brittle.
        return s.replaceAll("\\s+", " ");
    }
}
