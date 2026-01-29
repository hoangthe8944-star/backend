package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.LiveRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveRoomRepository extends MongoRepository<LiveRoom, String> {
    Optional<LiveRoom> findByHostIdAndStatus(String hostId, String status);
    List<LiveRoom> findByStatus(String status);
}
