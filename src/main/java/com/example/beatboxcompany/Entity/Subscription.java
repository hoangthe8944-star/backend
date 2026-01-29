package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Data // Lombok: tạo Getters, Setters, toString, equals, hashCode
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;
    
    // Tên gói (Ví dụ: "Basic", "Premium", "Family")
    private String name;
    
    // Mô tả gói
    private String description;
    
    // Giá tiền (sử dụng BigDecimal cho tiền tệ)
    private BigDecimal price;
    
    // Đơn vị giá (Ví dụ: "VND", "USD")
    private String currency;
    
    // Chu kỳ thanh toán (Ví dụ: P30D (30 ngày), P1Y (1 năm))
    // Sử dụng Duration hoặc String để lưu trữ chu kỳ
    private String duration; 

    // Các tính năng chính (Ví dụ: "Không quảng cáo", "Nghe offline")
    private List<String> features;
    
    // Trạng thái (Ví dụ: "ACTIVE", "INACTIVE")
    private String status; 
}