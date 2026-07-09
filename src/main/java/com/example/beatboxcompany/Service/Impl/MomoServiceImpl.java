package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Service.MomoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final RestTemplate restTemplate;

    // MoMo Sandbox Credentials
    private final String partnerCode = "MOMOLRJZ20181206";
    private final String accessKey = "mTCKt9W3eU1m39TW";
    private final String secretKey = "SetA5RDnLHvt51AULf51DyauxUo3kDU6";
    private final String momoApiUrl = "https://test-payment.momo.vn/v2/gateway/api/create";
    private final String redirectUrl = "https://hoangthe8944-star.github.io/webwithreactjs/#/premium-success";
    private final String ipnUrl = "https://beatbox-backend.ngrok-free.app/api/public/premium/momo-ipn";

    public MomoServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String createPaymentUrl(String orderId, long amount, String orderInfo, String extraData) throws Exception {
        String requestId = orderId;
        String amountStr = String.valueOf(amount);
        String requestType = "captureWallet";

        // Xây dựng raw signature string
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSha256(rawHash, secretKey);

        // Chuẩn bị payload gửi cho MoMo
        Map<String, Object> payload = new HashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("partnerName", "Beatbox Company");
        payload.put("storeId", "BeatboxStore");
        payload.put("requestId", requestId);
        payload.put("amount", amountStr);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("lang", "vi");
        payload.put("extraData", extraData);
        payload.put("requestType", requestType);
        payload.put("signature", signature);

        log.info("Sending payment request to MoMo for order: {}", orderId);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(momoApiUrl, payload, Map.class);
            if (response != null && response.containsKey("payUrl")) {
                return (String) response.get("payUrl");
            } else {
                String errorMsg = response != null && response.containsKey("message") ? (String) response.get("message") : "Unknown error";
                throw new RuntimeException("Lỗi tạo giao dịch MoMo: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("Error calling MoMo Gateway API", e);
            throw new RuntimeException("Không thể kết nối đến cổng thanh toán MoMo: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> ipnParams) throws Exception {
        String partnerCodeParam = ipnParams.get("partnerCode");
        String orderId = ipnParams.get("orderId");
        String requestId = ipnParams.get("requestId");
        String amount = ipnParams.get("amount");
        String orderInfo = ipnParams.get("orderInfo");
        String orderType = ipnParams.get("orderType");
        String transId = ipnParams.get("transId");
        String resultCode = ipnParams.get("resultCode");
        String message = ipnParams.get("message");
        String payType = ipnParams.get("payType");
        String responseTime = ipnParams.get("responseTime");
        String extraData = ipnParams.get("extraData");
        String signature = ipnParams.get("signature");

        // Xác thực chữ ký để bảo mật
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCodeParam +
                "&payType=" + payType +
                "&requestId=" + requestId +
                "&responseTime=" + responseTime +
                "&resultCode=" + resultCode +
                "&transId=" + transId;

        String calculatedSignature = hmacSha256(rawHash, secretKey);
        boolean isValid = calculatedSignature.equalsIgnoreCase(signature);
        if (!isValid) {
            log.warn("MoMo signature verification failed! Calculated: {}, Received: {}", calculatedSignature, signature);
        }
        return isValid;
    }

    private String hmacSha256(String data, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        sha256HMAC.init(secretKeySpec);
        byte[] rawHmac = sha256HMAC.doFinal(dataBytes);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : rawHmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
