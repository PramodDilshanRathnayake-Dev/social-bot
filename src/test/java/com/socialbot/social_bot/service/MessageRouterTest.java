package com.socialbot.social_bot.service;

import com.socialbot.social_bot.service.ai.AiResponder;
import com.socialbot.social_bot.service.faq.FaqService;
import com.socialbot.social_bot.service.greeting.GreetingService;
import com.socialbot.social_bot.service.order.OrderConversationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageRouterTest {

    @Test
    void routesHiToStaticGreeting_withoutFaqOrAi() {
        GreetingService greetingService = mock(GreetingService.class);
        FaqService faqService = mock(FaqService.class);
        AiResponder aiResponder = mock(AiResponder.class);
        OrderConversationService orderConversationService = mock(OrderConversationService.class);

        when(orderConversationService.maybeHandle(anyString(), anyString())).thenReturn(Optional.empty());
        when(greetingService.isHi("hi")).thenReturn(true);
        when(greetingService.reply()).thenReturn("HELLO_STATIC");

        MessageRouter router = new MessageRouter(orderConversationService, greetingService, faqService, aiResponder);

        String reply = router.route("94770000000", "  hi  ");
        assertEquals("HELLO_STATIC", reply);

        verify(faqService, never()).findAnswer(anyString());
        verify(aiResponder, never()).reply(anyString(), anyString());
    }

    @Test
    void routesKnownQuestionToDbAnswer_withoutAi() {
        GreetingService greetingService = mock(GreetingService.class);
        FaqService faqService = mock(FaqService.class);
        AiResponder aiResponder = mock(AiResponder.class);
        OrderConversationService orderConversationService = mock(OrderConversationService.class);

        when(orderConversationService.maybeHandle(anyString(), anyString())).thenReturn(Optional.empty());
        when(greetingService.isHi(anyString())).thenReturn(false);
        when(faqService.findAnswer("what is the price")).thenReturn(Optional.of("DB_ANSWER"));

        MessageRouter router = new MessageRouter(orderConversationService, greetingService, faqService, aiResponder);

        String reply = router.route("94770000000", "What   is   the   price");
        assertEquals("DB_ANSWER", reply);

        verify(aiResponder, never()).reply(anyString(), anyString());
    }

    @Test
    void routesUnknownToAiFallback() {
        GreetingService greetingService = mock(GreetingService.class);
        FaqService faqService = mock(FaqService.class);
        AiResponder aiResponder = mock(AiResponder.class);
        OrderConversationService orderConversationService = mock(OrderConversationService.class);

        when(orderConversationService.maybeHandle(anyString(), anyString())).thenReturn(Optional.empty());
        when(greetingService.isHi(anyString())).thenReturn(false);
        when(faqService.findAnswer(anyString())).thenReturn(Optional.empty());
        when(aiResponder.reply("94770000000", "random")).thenReturn("AI_ANSWER");

        MessageRouter router = new MessageRouter(orderConversationService, greetingService, faqService, aiResponder);

        String reply = router.route("94770000000", "random");
        assertEquals("AI_ANSWER", reply);
    }

    @Test
    void orderFlowOverridesOtherRouting() {
        GreetingService greetingService = mock(GreetingService.class);
        FaqService faqService = mock(FaqService.class);
        AiResponder aiResponder = mock(AiResponder.class);
        OrderConversationService orderConversationService = mock(OrderConversationService.class);

        when(orderConversationService.maybeHandle("94770000000", "hi")).thenReturn(Optional.of("ORDER_REPLY"));

        MessageRouter router = new MessageRouter(orderConversationService, greetingService, faqService, aiResponder);

        String reply = router.route("94770000000", "hi");
        assertEquals("ORDER_REPLY", reply);

        verifyNoInteractions(greetingService);
        verifyNoInteractions(faqService);
        verifyNoInteractions(aiResponder);
    }
}

