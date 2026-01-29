package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Entity.Comment;
import com.example.beatboxcompany.Dto.CommentDto;

public class CommentMapper {

    // Chuyển từ Entity sang Response DTO
    public static CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setSongId(comment.getSongId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}