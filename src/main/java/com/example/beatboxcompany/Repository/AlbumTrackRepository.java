package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.AlbumTrack;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AlbumTrackRepository extends MongoRepository<AlbumTrack, String> {

    List<AlbumTrack> findByAlbumIdOrderByTrackNumberAsc(String albumId);

    List<AlbumTrack> findByAlbumId(String albumId);
}
