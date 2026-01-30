package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.PremiumStatusDto;
import com.example.beatboxcompany.Request.PurchasePremiumRequest;

public interface PremiumService {

    /**
     * Lấy trạng thái Premium của user
     * 
     * @param userId ID của user
     * @return PremiumStatusDto chứa thông tin trạng thái premium
     */
    PremiumStatusDto getPremiumStatus(String userId);

    /**
     * Mua gói Premium
     * 
     * @param userId  ID của user
     * @param request Thông tin gói premium cần mua
     * @return PremiumStatusDto với thông tin premium mới
     */
    PremiumStatusDto purchasePremium(String userId, PurchasePremiumRequest request);

    /**
     * Hủy gói Premium
     * 
     * @param userId ID của user
     */
    void cancelPremium(String userId);
}
