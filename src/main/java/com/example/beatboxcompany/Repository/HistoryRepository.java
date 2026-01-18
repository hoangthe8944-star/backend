package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.History;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends MongoRepository<History, String> {
    // Tìm lịch sử của 1 user, sắp xếp bài mới nghe nhất lên đầu
    List<History> findByUserIdOrderByPlayedAtDesc(String userId);
    
    // Tìm xem user đã từng nghe bài này chưa để cập nhật thời gian
    Optional<History> findByUserIdAndSongId(String userId, String songId);
}