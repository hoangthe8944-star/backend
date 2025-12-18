package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List; // Nhớ import List

@Data
public class SongDto {
    private String id;
    private String title;
    
    // --- Các trường hiển thị (cho Frontend dễ dùng) ---
    private String artistName;
    private String albumName;
    
    // --- Các trường tham chiếu (bổ sung để sửa lỗi Mapper) ---
    private String artistId;
    private String albumId;
    
    // --- Các thông tin chi tiết khác ---
    private String coverUrl;
    private Integer duration;
    private String streamUrl;
    private String status;
    
    // --- Bổ sung các trường còn thiếu ---
    private List<String> genre;
    private Long viewCount;
    private Boolean isExplicit;
}