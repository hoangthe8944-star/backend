package com.example.beatboxcompany.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "episodes")
public class Episode {

    @Id
    private String id;
    private String podcastId;
    private String title;
    private String description;
    
    private String mediaUrl;
    private String mediaPublicId; // Cloudinary resource public ID
    private long durationMs;
    private long playCount = 0;
    
    private String status = "PUBLISHED"; // PENDING, PUBLISHED, etc.
    private String mediaType = "AUDIO"; // AUDIO or VIDEO

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
