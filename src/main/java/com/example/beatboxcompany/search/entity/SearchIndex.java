package com.example.beatboxcompany.search.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "search_index")
public class SearchIndex {

    @Id
    private String id;

    // Loại dữ liệu: SONG, ARTIST, ALBUM...
    @Indexed
    private String type;

    // ID tham chiếu đến entity gốc, ví dụ Song.id
    @Indexed
    private String referenceId;

    // ================== THÔNG TIN TÌM KIẾM ==================

    @TextIndexed(weight = 5)
    private String title;

    @TextIndexed(weight = 3)
    private String subtitle; // Thường là artistName

    @TextIndexed(weight = 3)
    private String artistName;

    @TextIndexed(weight = 2)
    private String description;

    private String searchText;

    @TextScore
    private Float score;

    // ================== THÔNG TIN LIÊN KẾT ==================

    private String artistId;

    private String albumId;

    private String categoryId;

    // ================== THÔNG TIN PHÁT NHẠC ==================

    // Field quan trọng frontend đang cần để play nhạc
    private String streamUrl;

    // Có thể dùng làm fallback nếu streamUrl không có
    private String audioUrl;

    // Nếu nhạc lưu local trong server
    private String filePath;

    // Public ID file MP3 trên Cloudinary
    private String streamPublicId;

    // ================== THÔNG TIN ẢNH ==================

    private String imageUrl;

    private String coverUrl;

    private String coverImageUrl;

    private String coverPublicId;

    // ================== METADATA BÀI HÁT ==================

    private int duration;

    private Long durationMs;

    @Builder.Default
    private List<String> genres = new ArrayList<>();

    @Builder.Default
    private long viewCount = 0L;

    @Builder.Default
    private Long popularity = 0L;

    @Builder.Default
    private Boolean explicit = false;

    @Builder.Default
    private String status = "PENDING";

    private String spotifyId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastPlayedAt;
}