package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Artist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends MongoRepository<Artist, String> {

    // Tìm hồ sơ Artist dựa trên tên công khai (ví dụ: cần cho Search)
    Optional<Artist> findByName(String name);

    List<Artist> findByNameIgnoreCase(String name);

    List<Artist> findByGenresContaining(String genre);

    List<Artist> findByNameContainingIgnoreCase(String name);
}