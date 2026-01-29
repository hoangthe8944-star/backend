package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Album;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface AlbumRepository extends MongoRepository<Album, String> {
    List<Album> findByStatus(String status);

    List<Album> findByArtistId(String artistId);

    Optional<Album> findByTitleAndArtistId(String title, String artistId);
    
}