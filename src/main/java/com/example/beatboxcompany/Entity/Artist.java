package com.example.beatboxcompany.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime; 
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "artists")
public class Artist {

    @Id
    private String id; // ID từ Spotify (ví dụ: 0TnOYISj6SdbuYM5fSsg8O)

    @Indexed(unique = true)
    private String userId; // Link tới tài khoản User sở hữu hồ sơ này

    private String name;
    private String bio;

    // --- CÁC TRƯỜNG ẢNH ---
    
    // 1. Ảnh đại diện chính (Thường lấy tấm images[1] - 320x320 hoặc images[0] - 640x640)
    private String avatarUrl;
    
    // 2. Ảnh bìa lớn (Banner nằm ngang ở trang cá nhân nghệ sĩ)
    private String coverImageUrl; 

    // 3. Danh sách tất cả các ảnh Spotify trả về (đủ các size: 640, 320, 160)
    // Lưu dưới dạng mảng String hoặc Object tùy bạn, ở đây dùng String URL cho đơn giản
    private List<String> images = new ArrayList<>();

    // --- THÔNG TIN KHÁC ---
    private long followerCount = 0;

    // === KẾT NỐI VỚI CATEGORY ===
    private List<String> genres = new ArrayList<>();

    // === CÁC TRƯỜNG TRẠNG THÁI ===
    private boolean verified = false;        
    private LocalDateTime createdAt = LocalDateTime.now(); 
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Inner class nếu bạn muốn lưu chi tiết kích thước ảnh (Tùy chọn)
    /*
    @Data
    public static class ImageInfo {
        private String url;
        private int width;
        private int height;
    }
    */
}