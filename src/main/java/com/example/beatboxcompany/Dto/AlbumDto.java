package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List; // Nhớ import

@Data
public class AlbumDto {
    private String id;
    private String title;
    private String coverUrl;
    private LocalDate releaseDate;
    private String status;

    private String artistName;
    private Integer songCount;    
    private String totalDuration; 
    private Long totalStreams;
    
    // Thêm trường này để Mapper toFullDto hoạt động
    private List<TrackDto> tracks; 
    private String rejectedReason; // Lý do từ chối (cho Admin)
}