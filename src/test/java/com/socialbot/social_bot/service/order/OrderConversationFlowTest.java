package com.socialbot.social_bot.service.order;

import com.socialbot.social_bot.model.ClientOrder;
import com.socialbot.social_bot.repository.ClientOrderRepository;
import com.socialbot.social_bot.service.whatsapp.WhatsAppSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@TestPropertySource(properties = {
        "whatzzi.admin-number=+94717465220",
        "whatzzi.packages[0].code=BASIC",
        "whatzzi.packages[0].name=Basic Bot",
        "whatzzi.packages[0].price-minor=15000",
        "whatzzi.packages[0].currency=LKR",
        "whatzzi.packages[0].description=Standard WhatsApp chatbot"
})
class OrderConversationFlowTest {

    @Autowired
    OrderConversationService orderConversationService;

    @Autowired
    ClientOrderRepository clientOrderRepository;

    @MockBean
    WhatsAppSender whatsAppSender;

    @BeforeEach
    void setUp() {
        clientOrderRepository.deleteAll();
    }

    @Test
    void happyPath_requiresConfirm_andNotifiesAdminOnSubmit() {
        String from = "94770000000";

        String r1 = orderConversationService.maybeHandle(from, "order").orElseThrow();
        assertTrue(r1.toLowerCase().contains("choose a package"));

        String r2 = orderConversationService.maybeHandle(from, "BASIC").orElseThrow();
        assertTrue(r2.toLowerCase().contains("whatsapp business"));

        String r3 = orderConversationService.maybeHandle(from, "+94770000001").orElseThrow();
        assertTrue(r3.toLowerCase().contains("personal"));

        String r4 = orderConversationService.maybeHandle(from, "+94770000002").orElseThrow();
        assertTrue(r4.toLowerCase().contains("order summary"));
        assertTrue(r4.contains("CONFIRM"));

        String r5 = orderConversationService.maybeHandle(from, "CONFIRM").orElseThrow();
        assertTrue(r5.toLowerCase().contains("submitted"));

        ClientOrder saved = clientOrderRepository.findAll().getFirst();
        assertEquals(ClientOrder.Status.SUBMITTED, saved.getStatus());

        verify(whatsAppSender).sendTextMessage(eq("+94717465220"), contains("NEW ORDER SUBMITTED"));
    }

    @Test
    void failurePath_invalidPhone_doesNotAdvance() {
        String from = "94770000000";

        orderConversationService.maybeHandle(from, "order").orElseThrow();
        orderConversationService.maybeHandle(from, "BASIC").orElseThrow();

        String r = orderConversationService.maybeHandle(from, "not-a-number").orElseThrow();
        assertTrue(r.toLowerCase().contains("invalid number"));

        verifyNoInteractions(whatsAppSender);
    }

    @Test
    void failurePath_notConfirm_doesNotSubmit() {
        String from = "94770000000";

        orderConversationService.maybeHandle(from, "order").orElseThrow();
        orderConversationService.maybeHandle(from, "BASIC").orElseThrow();
        orderConversationService.maybeHandle(from, "+94770000001").orElseThrow();
        orderConversationService.maybeHandle(from, "+94770000002").orElseThrow();

        String r = orderConversationService.maybeHandle(from, "yes").orElseThrow();
        assertTrue(r.toLowerCase().contains("reply confirm"));

        ClientOrder saved = clientOrderRepository.findAll().getFirst();
        assertEquals(ClientOrder.Status.AWAITING_CONFIRMATION, saved.getStatus());

        verifyNoInteractions(whatsAppSender);
    }
}
