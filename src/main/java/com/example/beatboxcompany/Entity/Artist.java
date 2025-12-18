package com.example.beatboxcompany.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime; 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "artists")
public class Artist {

    @Id
    private String id; // ID hồ sơ Nghệ sĩ (dùng để bài hát tham chiếu)

    // Liên kết 1-1: ID của tài khoản User quản lý hồ sơ này
    @Indexed(unique = true)
    private String userId;

    private String name; // Tên công khai của Nghệ sĩ
    private String bio; // Tiểu sử
    private String avatarUrl;

    private long followerCount = 0; // Số lượng người theo dõi

    // === THÊM 2 TRƯỜNG BỊ THIẾU VÀO ĐÂY ===
    private LocalDateTime createdAt; // Thời gian tạo hồ sơ
    private boolean verified;        // Trạng thái đã xác thực hay chưa

}