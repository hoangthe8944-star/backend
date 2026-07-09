package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdvertisementDto {
    private String id;
    private String title;
    private String partnerName;
    private String adType;
    private String mediaUrl;
    private String audioUrl;
    private String imageUrl;
    private String targetUrl;
    private int duration;
    private boolean isActive;
    private long views;
    private long clicks;
    private LocalDateTime createdAt;

    // Alias fields for frontend compatibility
    private String image;
    private String videoUrl;
    private String mp4;
    private String audio;
}
