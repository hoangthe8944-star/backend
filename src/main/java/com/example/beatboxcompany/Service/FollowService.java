package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.FollowDto;

import java.util.List;

public interface FollowService {
    void follow(String followerId, String targetId, String targetType);
    
    void unfollow(String followerId, String targetId, String targetType);
    
    boolean isFollowing(String followerId, String targetId, String targetType);
    
    List<FollowDto> getFollowers(String targetId, String targetType);
    
    List<FollowDto> getFollowing(String followerId, String targetType);
    
    long getFollowerCount(String targetId, String targetType);
    
    long getFollowingCount(String followerId, String targetType);
}