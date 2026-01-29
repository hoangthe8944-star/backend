package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoryDto {
    private String id;
    private String userId;
    private String songId;
    private LocalDateTime playedAt;
    private SongDto songDetails; // Chứa tên bài hát, ảnh cover, artist...
}