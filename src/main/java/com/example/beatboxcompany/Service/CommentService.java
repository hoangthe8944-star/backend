package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.CommentDto;
import com.example.beatboxcompany.Request.CommentRequest;

import java.util.List;

public interface CommentService {
    
    // 1. Tạo bình luận mới
    CommentDto createComment(String songId, CommentRequest request, String currentUserId);
    
    // 2. Lấy tất cả bình luận cho một bài hát
    List<CommentDto> getCommentsBySongId(String songId);
    
    // 3. Xóa bình luận (chỉ chủ sở hữu hoặc Admin)
    void deleteComment(String commentId, String currentUserId);

    // 4. Admin xóa bất kỳ bình luận nào
    void adminDeleteComment(String commentId);
}