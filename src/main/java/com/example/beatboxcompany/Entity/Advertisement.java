package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "advertisements")
public class Advertisement {
    @Id
    private String id;
    private String title;
    private String partnerName;
    
    // "VIDEO" (MP4) or "AUDIO_IMAGE" (MP3 & Image)
    private String adType; 
    
    private String mediaUrl; // Video URL (MP4)
    private String audioUrl; // Audio URL (MP3)
    private String imageUrl; // Banner Image URL
    private String targetUrl; // Click redirect link
    
    private int duration; // In seconds
    private boolean isActive = true;
    
    private long views = 0;
    private long clicks = 0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Alias fields for frontend compatibility
    private String image;
    private String videoUrl;
    private String mp4;
    private String audio;
}
