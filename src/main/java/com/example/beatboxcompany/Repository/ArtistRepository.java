package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Artist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends MongoRepository<Artist, String> {
    
    // Tìm hồ sơ Artist dựa trên ID tài khoản User quản lý (cần thiết cho việc chỉnh sửa)
    Optional<Artist> findByUserId(String userId);

    // Tìm hồ sơ Artist dựa trên tên công khai (ví dụ: cần cho Search)
    Optional<Artist> findByName(String name);

    Optional<Artist> findByNameIgnoreCase(String name);
}