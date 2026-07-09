package com.example.beatboxcompany.Repository;

import com.example.beatboxcompany.Entity.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<Follow, String> {
    
    Optional<Follow> findByFollowerIdAndTargetIdAndTargetType(String followerId, String targetId, Follow.TargetType targetType);
    
    List<Follow> findByFollowerIdAndTargetType(String followerId, Follow.TargetType targetType);
    
    List<Follow> findByTargetIdAndTargetType(String targetId, Follow.TargetType targetType);
    
    long countByTargetIdAndTargetType(String targetId, Follow.TargetType targetType);
    
    long countByFollowerIdAndTargetType(String followerId, Follow.TargetType targetType);
    
    void deleteByFollowerIdAndTargetIdAndTargetType(String followerId, String targetId, Follow.TargetType targetType);
    
    boolean existsByFollowerIdAndTargetIdAndTargetType(String followerId, String targetId, Follow.TargetType targetType);
}