package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Episode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends MongoRepository<Episode, String> {
    List<Episode> findByPodcastId(String podcastId);
}
