package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Playlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends MongoRepository<Playlist, String> {
    
    // Dùng cho UserController (lấy tất cả Playlist của người dùng)
    List<Playlist> findByOwnerId(String ownerId);

    // Dùng cho PublicController (lấy các Playlist công khai)
    Optional<Playlist> findByIdAndIsPublic(String id, boolean isPublic);
}