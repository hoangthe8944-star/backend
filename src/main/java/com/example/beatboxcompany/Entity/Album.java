package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "albums")
@Data
public class Album {
    @Id
    private String id;
    private String title;
    private LocalDate releaseDate;
    private String coverUrl;
    private String artistId;
    private Integer totalTracks = 0;
    private Long totalDurationMs = 0L;
    private String status = "PENDING";
    private String rejectedReason;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Thay vì bảng trung gian → lưu thẳng danh sách songId
    private List<String> trackIds = new ArrayList<>();
}