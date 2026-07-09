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
@Document(collection = "follows")
public class Follow {
    @Id
    private String id;
    
    private String followerId; // ID của User đang theo dõi
    
    private String targetId; // ID của User hoặc Artist bị theo dõi
    
    private TargetType targetType; // Loại đối tượng bị theo dõi (USER hoặc ARTIST)
    
    private LocalDateTime createdAt;
    
    public enum TargetType {
        USER,
        ARTIST
    }
}