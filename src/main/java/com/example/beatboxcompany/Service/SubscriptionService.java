package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.SubscriptionDto;
import java.util.List;

public interface SubscriptionService {
    
    // Admin: Lấy danh sách tất cả các gói
    List<SubscriptionDto> getAllSubscriptions();
    
    // Admin: Tạo gói thành viên mới
    SubscriptionDto createSubscription(SubscriptionDto subscriptionDto);

    // Admin: Cập nhật thông tin gói
    SubscriptionDto updateSubscription(String id, SubscriptionDto subscriptionDto);
    
    // Admin: Xóa gói thành viên
    void deleteSubscription(String id);
}