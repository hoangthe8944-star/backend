package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.PremiumStatusResponse;
import com.example.beatboxcompany.Dto.SubscriptionDto;
import java.util.List;

public interface PremiumService {
    List<SubscriptionDto> getAvailablePackages();
    PremiumStatusResponse getPremiumStatus(String userId);
    PremiumStatusResponse subscribe(String userId, String packageId);
    PremiumStatusResponse cancelSubscription(String userId);
    String initiateMomoPayment(String userId, String packageId) throws Exception;
    void processMomoIPN(java.util.Map<String, String> ipnParams) throws Exception;
}
