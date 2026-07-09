package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Advertisement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends MongoRepository<Advertisement, String> {
    List<Advertisement> findByIsActive(boolean isActive);
}
