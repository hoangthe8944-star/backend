package com.example.beatboxcompany.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments") 
public class Comment {

    @Id
    private String id;
    
    // ID của bài hát mà bình luận này thuộc về
    private String songId; 
    
    // ID của người dùng đã tạo bình luận
    private String userId; 
    
    private String content;
    
    // Thời gian tạo bình luận
    private Instant createdAt = Instant.now(); 
}