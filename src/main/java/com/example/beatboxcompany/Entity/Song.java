package com.example.beatboxcompany.Entity;

import lombok.*; // Import đầy đủ Lombok
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "songs")
@Data
@Builder // [QUAN TRỌNG] Thêm dòng này để sửa lỗi .builder() ở các file khác
@NoArgsConstructor // [QUAN TRỌNG] Cần thiết cho MongoDB khi map dữ liệu
@AllArgsConstructor // [QUAN TRỌNG] Cần thiết để @Builder hoạt động
public class Song {
    @Id
    private String id;
    @TextIndexed
    private String title;
    @TextIndexed
    private String artistName; 
    private String artistId;
    private String albumId;
     private String categoryId; // ID danh mục bài hát

    private int duration;
    private Long durationMs; // Đơn vị: mili-giây

    private String streamUrl; // Link ngoài (nếu có)
    private String coverUrl;

    // Khởi tạo mặc định để tránh NullPointerException khi getGenre().add(...)
    @Builder.Default
    private List<String> genre = new ArrayList<>();

    @Builder.Default
    private long viewCount = 0L;

    @Builder.Default
    private Boolean isExplicit = false;

    @Builder.Default
    private String status = "PENDING";

    private String streamPublicId; // Public ID của file MP3 trên Cloudinary
    private String coverPublicId; // Public ID của file ảnh bìa trên Cloudinary

    // --- Các trường quan trọng bạn đã thêm (Giữ nguyên) ---
    private String filePath; // Tên file trên ổ cứng (uploads/xyz.mp3)
    private String spotifyId; // ID Spotify để tránh trùng lặp

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastPlayedAt; // TRƯỜNG MỚI ĐỂ LƯU THỜI GIAN NGHE GẦN NHẤT
}