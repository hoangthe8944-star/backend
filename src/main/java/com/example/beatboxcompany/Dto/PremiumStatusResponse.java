package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PremiumStatusResponse {
    private String userId;
    private String username;
    private String email;
    private Long coins;
    
    private boolean isPremium;
    private String premiumType;
    private LocalDateTime premiumExpiresAt;
    private long daysRemaining;
    
    private BigDecimal price;
    private String currency;
    private String duration;
    private List<String> activeFeatures;
    private String statusDescription;
}
