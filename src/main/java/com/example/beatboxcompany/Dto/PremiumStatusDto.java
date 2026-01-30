package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumStatusDto {
    private Boolean isPremium;
    private String premiumType; // "monthly" hoặc "yearly"
    private LocalDateTime premiumExpiry;
    private Long daysRemaining; // Số ngày còn lại
}
