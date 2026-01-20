package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.LiveRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// Đổi JpaRepository thành MongoRepository
public interface LiveRoomRepository extends MongoRepository<LiveRoom, String> {
    List<LiveRoom> findByIsLiveTrue();
}