package com.example.beatboxcompany.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.NoArgsConstructor; // <--- Import cái này
import lombok.AllArgsConstructor;
import java.util.List;

@Builder
@Data
@NoArgsConstructor  // <--- QUAN TRỌNG: Tạo public User() {} để sửa lỗi trong Mapper
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    
    // ĐỔI TỪ role -> roles
    private List<String> roles; 

    private String avatarUrl;
    private List<String> likedSongs;
    private List<String> followedArtists;
}