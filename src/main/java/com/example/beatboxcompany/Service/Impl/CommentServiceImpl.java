package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.Comment;
import com.example.beatboxcompany.Dto.CommentDto;
import com.example.beatboxcompany.Request.CommentRequest;
import com.example.beatboxcompany.Mapper.CommentMapper;
import com.example.beatboxcompany.Repository.CommentRepository;
import com.example.beatboxcompany.Service.CommentService;
import com.example.beatboxcompany.Service.SongService; // Cần kiểm tra bài hát

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final SongServiceImpl songService; 
    
    public CommentServiceImpl(CommentRepository commentRepository, SongServiceImpl songService) {
        this.commentRepository = commentRepository;
        this.songService = songService;
    }

    @Override
    public CommentDto createComment(String songId, CommentRequest request, String currentUserId) {
        // 1. Nghiệp vụ quan trọng: Đảm bảo bài hát tồn tại và đã PUBLISHED
        songService.getPublishedSongById(songId); 
        
        // 2. Tạo Entity
        Comment comment = new Comment();
        comment.setSongId(songId);
        comment.setUserId(currentUserId); 
        comment.setContent(request.getContent());
        comment.setCreatedAt(Instant.now());

        // 3. Lưu và trả về
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsBySongId(String songId) {
        // [Nghiệp vụ: PublicController sẽ gọi SongService trước để đảm bảo bài hát PUBLISHED]
        
        return commentRepository.findBySongIdOrderByCreatedAtAsc(songId).stream()
            .map(CommentMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(String commentId, String currentUserId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại."));

        // Kiểm tra quyền: Chỉ chủ sở hữu mới được xóa
        if (!comment.getUserId().equals(currentUserId)) {
            throw new SecurityException("Bạn không có quyền xóa bình luận này.");
        }
        
        commentRepository.delete(comment);
    }

    @Override
    public void adminDeleteComment(String commentId) {
        // Phương thức này chỉ được gọi từ AdminController (đã được bảo vệ)
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Bình luận không tồn tại.");
        }
        commentRepository.deleteById(commentId);
    }
}