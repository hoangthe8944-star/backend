package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    // Đổi tên thành 'roles' để khớp với Frontend authapi.ts
    private List<String> roles; 
    
    // Thêm trường này để Frontend biết user đã được phép nghe nhạc chưa
    private boolean isVerified;

    // Constructor tùy chỉnh (nếu bạn không muốn dùng @AllArgsConstructor)
    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles, boolean isVerified) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isVerified = isVerified;
    }
}