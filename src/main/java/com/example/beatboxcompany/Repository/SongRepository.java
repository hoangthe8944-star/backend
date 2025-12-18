package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Song;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends MongoRepository<Song, String> {

    // Dùng cho ArtistController (Xem bài hát đã upload)
    List<Song> findByArtistId(String artistId);

    // Dùng cho AdminController (Lấy danh sách chờ duyệt)
    List<Song> findByStatus(String status);

    // Dùng cho PublicController (Bài hát đã PUBLISHED)
    List<Song> findByStatusAndAlbumId(String status, String albumId);

    // Dùng cho PublicController (Bài hát Hot)
    List<Song> findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(String titleKeyword,
            String artistKeyword);

    boolean existsByTitleIgnoreCaseAndArtistNameIgnoreCase(String title, String artistName);

    List<Song> findByStatusAndLastPlayedAtIsNotNullOrderByLastPlayedAtDesc(String status);
}