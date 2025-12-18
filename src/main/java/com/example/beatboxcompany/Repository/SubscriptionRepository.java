package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    // Phương thức tìm kiếm theo tên (có thể hữu ích cho Admin)
    Optional<Subscription> findByName(String name);
}