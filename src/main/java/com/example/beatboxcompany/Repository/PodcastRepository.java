package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Podcast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PodcastRepository extends MongoRepository<Podcast, String> {
    List<Podcast> findByHostId(String hostId);
    List<Podcast> findByCategoriesContaining(String category);
}
