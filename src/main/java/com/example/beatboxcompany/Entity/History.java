package com.example.beatboxcompany.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "listening_history")
public class History {
    @Id
    private String id;
    private String userId;
    private String songId;
    private LocalDateTime playedAt;
}