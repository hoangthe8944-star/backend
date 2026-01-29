package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Request.UserRegisterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    // Chuyển từ Request đăng ký -> Entity (User)
    public static User toEntity(UserRegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());

        // --- SỬA 1: Xử lý Roles (Số nhiều) ---
        // Logic: Nếu null thì mặc định ROLE_USER, nếu có thì thêm tiền tố ROLE_
        if (request.getRole() == null || request.getRole().isEmpty()) {
            user.setRoles(List.of("ROLE_USER")); // Sửa setRole -> setRoles
        } else {
            List<String> processedRoles = request.getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toList());
            user.setRoles(processedRoles); // Sửa setRole -> setRoles
        }
        
        // --- SỬA 2: Mật khẩu ở đâu? ---
        // Mapper KHÔNG NÊN mã hóa mật khẩu (việc đó của Service).
        // Nhưng Mapper CẦN chuyển mật khẩu thô sang Entity để Service mã hóa sau.
        // Hoặc: Service tự setPassword sau khi map.
        // Ở đây mình để trống password, nhưng bạn phải nhớ set nó trong UserService!

        return user;
    }

    // Chuyển từ Entity -> DTO (Trả về cho Client)
    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        
        // --- SỬA 3: Đồng bộ tên biến roles ---
        dto.setRoles(user.getRoles()); // Sửa getRole -> getRoles, setRole -> setRoles
        
        dto.setAvatarUrl(user.getAvatarUrl());
        
        // --- SỬA 4: Null Safety (Tránh lỗi NullPointerException cho Frontend) ---
        dto.setLikedSongs(user.getLikedSongs() != null ? user.getLikedSongs() : new ArrayList<>());
        dto.setFollowedArtists(user.getFollowedArtists() != null ? user.getFollowedArtists() : new ArrayList<>());
        
        return dto;
    }
}