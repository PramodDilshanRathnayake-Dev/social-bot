package com.socialbot.social_bot.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class WebhookSignatureVerifier {

    private final byte[] secret;

    public WebhookSignatureVerifier(String appSecret) {
        this.secret = (appSecret == null) ? new byte[0] : appSecret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean verify(byte[] requestBody, String signatureHeader) {
        if (secret.length == 0) return false;
        if (requestBody == null) requestBody = new byte[0];
        if (signatureHeader == null) return false;

        String expected = sign(requestBody);
        return constantTimeEquals(expected, signatureHeader.trim());
    }

    // Test helper: produces a header in Meta format: "sha256=<hex>".
    public String signForTest(byte[] requestBody) {
        if (requestBody == null) requestBody = new byte[0];
        return sign(requestBody);
    }

    private String sign(byte[] requestBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] hmac = mac.doFinal(requestBody);
            return "sha256=" + toHex(hmac);
        } catch (Exception e) {
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        final char[] alphabet = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = alphabet[v >>> 4];
            hex[i * 2 + 1] = alphabet[v & 0x0F];
        }
        return new String(hex);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int diff = 0;
        for (int i = 0; i < x.length; i++) {
            diff |= x[i] ^ y[i];
        }
        return diff == 0;
    }
}

