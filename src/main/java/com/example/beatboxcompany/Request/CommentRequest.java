package com.example.beatboxcompany.Request;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    // Không cần songId hay userId ở đây, vì chúng ta sẽ lấy từ URL và Security Context
}