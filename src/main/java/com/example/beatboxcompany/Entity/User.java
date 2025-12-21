package com.example.beatboxcompany.Entity;

// <--- Import cái này
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor // <--- QUAN TRỌNG: Tạo public User() {} để sửa lỗi trong Mapper
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
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

    // Xác thực người dùng qua email
    private boolean isVerified = false; // Trạng thái xác thực
    private String verificationToken; // Mã xác thực ngẫu nhiên
    private LocalDateTime tokenExpiry; // Thời gian hết hạn mã
    private List<String> linkedEmails = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .filter(role -> role != null && !role.isEmpty()) // ✅ Lọc bỏ null/rỗng
                .map(role -> new SimpleGrantedAuthority(role)) // Không được để role là ""
                .collect(Collectors.toList());
    }
}