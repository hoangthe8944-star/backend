package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Kiểm tra email đã tồn tại chưa (dùng cho Register)
    boolean existsByEmail(String email);

    // Tìm user bằng mã xác thực (dùng cho Verify)
    Optional<User> findByVerificationToken(String token);
}
