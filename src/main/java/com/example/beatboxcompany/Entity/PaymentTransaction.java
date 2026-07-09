package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payment_transactions")
public class PaymentTransaction {
    @Id
    private String id;
    private String userId;
    private String packageId;
    private String packageName;
    private BigDecimal amount;
    private String orderId;
    private String status; // "PENDING", "SUCCESS", "FAILED"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
