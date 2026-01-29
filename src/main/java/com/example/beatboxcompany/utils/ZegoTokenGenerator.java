package com.example.beatboxcompany.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class ZegoTokenGenerator {

    public static String generateToken(
            long appId,
            String serverSecret,
            String userId,
            int effectiveTimeInSeconds
    ) {
        long now = System.currentTimeMillis() / 1000;
        long expire = now + effectiveTimeInSeconds;

        String nonce = UUID.randomUUID().toString().replace("-", "");

        String content = appId + userId + nonce + expire;

        String signature = hmacSha256(content, serverSecret);

        String token = appId + ":" + userId + ":" + nonce + ":" + expire + ":" + signature;

        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Generate Zego token failed", e);
        }
    }
}
