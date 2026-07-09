package com.example.beatboxcompany.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "podcasts")
public class Podcast {

    @Id
    private String id;
    private String title;
    private String description;
    private String coverImageUrl;
    private String hostId; // Creator / Host User ID
    private List<String> categories = new ArrayList<>();
    private List<String> episodeIds = new ArrayList<>(); // List of Episode IDs
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
