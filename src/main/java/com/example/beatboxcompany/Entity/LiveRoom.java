package com.example.beatboxcompany.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "live_rooms") // Dùng cái này cho MongoDB
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoom {
    @Id
    private String roomId;
    
    private String hostId;
    private String hostName;
    private String roomTitle;
    private boolean isLive;
    private LocalDateTime startedAt;
}