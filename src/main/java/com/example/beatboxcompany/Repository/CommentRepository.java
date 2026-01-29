package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    // Lấy tất cả bình luận cho một bài hát (dùng cho PublicController)
    List<Comment> findBySongIdOrderByCreatedAtAsc(String songId);
    
    // Lấy tất cả bình luận của một người dùng (dùng cho Admin/User quản lý)
    List<Comment> findByUserId(String userId);
}