package com.example.beatboxcompany.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LyricsDto {
    private Long id;
    private String trackName;
    private String artistName;
    private String albumName;
    private Integer duration;
    private String plainLyrics;  // Lời bài hát dạng văn bản thuần
    private String syncedLyrics; // Lời bài hát có mốc thời gian [00:10.00] (Dùng để chạy chữ)
}