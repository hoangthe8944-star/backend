package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CommentDto {
    private String id;
    private String songId;
    private String userId;
    // TODO: Nên thêm thông tin Username hoặc Avatar của User vào DTO này
    private String content;
    private Instant createdAt;
}