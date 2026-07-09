package com.example.beatboxcompany.Service;

import java.util.Map;

public interface MomoService {
    String createPaymentUrl(String orderId, long amount, String orderInfo, String extraData) throws Exception;
    boolean verifySignature(Map<String, String> ipnParams) throws Exception;
}
