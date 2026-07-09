package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Album;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface AlbumRepository extends MongoRepository<Album, String> {
    List<Album> findByStatus(String status);

    List<Album> findByArtistIdsContaining(String artistId);

    Optional<Album> findByNameAndArtistIdsContaining(String name, String artistId);
    
}