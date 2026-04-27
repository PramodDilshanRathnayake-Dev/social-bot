package com.socialbot.social_bot.controller;

import com.socialbot.social_bot.config.WhatsAppConfig;
import com.socialbot.social_bot.security.WebhookSecurityConfiguration;
import com.socialbot.social_bot.security.WebhookSignatureVerifier;
import com.socialbot.social_bot.service.WebhookProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@Import({WebhookSecurityConfiguration.class, WebhookControllerSignatureTest.PropsConfig.class})
@TestPropertySource(properties = {
        "whatsapp.meta.app-secret=topsecret",
        "whatsapp.meta.signature-verification-enabled=true"
})
class WebhookControllerSignatureTest {

    @TestConfiguration
    @EnableConfigurationProperties(WhatsAppConfig.class)
    static class PropsConfig {}

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WebhookProcessor webhookProcessor;

    @Test
    void rejectsWhenSignatureInvalid() throws Exception {
        byte[] body = "{\"entry\":[]}".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=deadbeef")
                        .content(body))
                .andExpect(status().isUnauthorized());

        verify(webhookProcessor, never()).processIncomingWebhook(anyMap());
    }

    @Test
    void acceptsWhenSignatureValid() throws Exception {
        byte[] body = "{\"entry\":[]}".getBytes(StandardCharsets.UTF_8);
        String header = new WebhookSignatureVerifier("topsecret").signForTest(body);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", header)
                        .content(body))
                .andExpect(status().isOk());

        verify(webhookProcessor).processIncomingWebhook(anyMap());
    }
}
