package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private String id;
    private String username;
    private String email;
    
    // Sửa thành số nhiều cho đúng bản chất List
    private List<String> roles; 
    
    private String avatarUrl;
    private List<String> likedSongs;
    private List<String> followedArtists;
    
    // Nếu bạn cần Constructor rỗng hoặc full tham số, 
    // @Data đôi khi chưa đủ nếu có logic phức tạp, 
    // nhưng cơ bản thế này là ổn.
    public UserDto() {} // Constructor mặc định cho Hibernate/Jackson
    
    // Constructor tiện lợi để convert từ Entity sang DTO
    public UserDto(String id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}