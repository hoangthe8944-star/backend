package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor  // Bắt buộc phải có để Jackson có thể chuyển đổi JSON
@AllArgsConstructor // Tạo constructor có đầy đủ tất cả các tham số
public class JwtResponse {
    
    private String token; 
    private String type = "Bearer"; 
    private String id;
    private String username;
    private String email;
    private List<String> roles; 
    private boolean isVerified;
    
    // Premium fields
    private boolean isPremium;
    private String premiumType;
    private LocalDateTime premiumExpiresAt;

    // Constructor tùy chỉnh
    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles, boolean isVerified) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isVerified = isVerified;
    }

    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles, boolean isVerified, boolean isPremium, String premiumType, LocalDateTime premiumExpiresAt) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isVerified = isVerified;
        this.isPremium = isPremium;
        this.premiumType = premiumType;
        this.premiumExpiresAt = premiumExpiresAt;
    }
}