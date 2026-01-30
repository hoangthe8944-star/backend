package com.example.beatboxcompany.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePremiumRequest {
    private String premiumType; // "monthly" hoặc "yearly" (bắt buộc)
    private String paymentMethod; // Phương thức thanh toán (tùy chọn, dành cho tương lai)
    private String paymentToken; // Token từ payment gateway (tùy chọn, dành cho tương lai)
}
