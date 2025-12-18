package com.example.beatboxcompany.Dto;

import lombok.Data;

import java.util.List;

@Data // Tự động sinh Getter, Setter, Constructor...
public class JwtResponse {
    
    // JWT Token mà Client cần lưu trữ và gửi kèm trong các Request sau này
    private String token; 
    
    // Loại token (mặc định là "Bearer")
    private String type = "Bearer"; 
    
    // Thông tin cơ bản của người dùng
    private String id;
    private String username;
    private String email;
    
    // Danh sách các vai trò (Roles) của người dùng (ví dụ: ["ROLE_USER", "ROLE_ARTIST"])
    private List<String> role; 

    // Constructor (để tiện tạo đối tượng sau khi đăng nhập)
    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = roles;
    }
}