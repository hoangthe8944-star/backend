package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDto {
    private String id;
    private String followerId;
    private String targetId;
    private String targetType;
    private LocalDateTime createdAt;
    
    // Thông tin của đối tượng được follow (Target details)
    private String targetName;
    private String targetAvatarUrl;
    
    // Thông tin của người follow (Follower details)
    private String followerName;
    private String followerAvatarUrl;
}