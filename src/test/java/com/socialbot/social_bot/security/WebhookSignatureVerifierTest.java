package com.socialbot.social_bot.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureVerifierTest {

    @Test
    void verifiesValidSha256SignatureHeader() {
        WebhookSignatureVerifier verifier = new WebhookSignatureVerifier("topsecret");

        byte[] body = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);
        String header = verifier.signForTest(body);

        assertTrue(verifier.verify(body, header));
    }

    @Test
    void rejectsInvalidSignature() {
        WebhookSignatureVerifier verifier = new WebhookSignatureVerifier("topsecret");
        byte[] body = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);

        assertFalse(verifier.verify(body, "sha256=deadbeef"));
    }
}

