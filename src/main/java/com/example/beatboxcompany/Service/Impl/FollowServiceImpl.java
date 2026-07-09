package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.FollowDto;
import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Entity.Follow;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Repository.FollowRepository;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Service.FollowService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    public FollowServiceImpl(FollowRepository followRepository,
                             UserRepository userRepository,
                             ArtistRepository artistRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
    }

    @Override
    public void follow(String followerId, String targetId, String targetType) {
        if (followerId.equals(targetId) && targetType.equalsIgnoreCase("USER")) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        Follow.TargetType type;
        try {
            type = Follow.TargetType.valueOf(targetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid target type. Must be USER or ARTIST");
        }

        boolean exists = followRepository.existsByFollowerIdAndTargetIdAndTargetType(followerId, targetId, type);
        if (exists) {
            return;
        }

        // Tạo bản ghi Follow mới
        Follow follow = Follow.builder()
                .followerId(followerId)
                .targetId(targetId)
                .targetType(type)
                .createdAt(LocalDateTime.now())
                .build();
        followRepository.save(follow);

        // Đồng bộ hóa với User và Artist nếu targetType là ARTIST
        if (type == Follow.TargetType.ARTIST) {
            userRepository.findById(followerId).ifPresent(user -> {
                if (user.getFollowedArtists() == null) {
                    user.setFollowedArtists(new ArrayList<>());
                }
                if (!user.getFollowedArtists().contains(targetId)) {
                    user.getFollowedArtists().add(targetId);
                    userRepository.save(user);
                }
            });

            artistRepository.findById(targetId).ifPresent(artist -> {
                artist.setFollowers(artist.getFollowers() + 1);
                artist.setFollowerCount(artist.getFollowerCount() + 1);
                artistRepository.save(artist);
            });
        }
    }

    @Override
    public void unfollow(String followerId, String targetId, String targetType) {
        Follow.TargetType type;
        try {
            type = Follow.TargetType.valueOf(targetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid target type. Must be USER or ARTIST");
        }

        boolean exists = followRepository.existsByFollowerIdAndTargetIdAndTargetType(followerId, targetId, type);
        if (!exists) {
            return;
        }

        followRepository.deleteByFollowerIdAndTargetIdAndTargetType(followerId, targetId, type);

        // Đồng bộ hóa với User và Artist nếu targetType là ARTIST
        if (type == Follow.TargetType.ARTIST) {
            userRepository.findById(followerId).ifPresent(user -> {
                if (user.getFollowedArtists() != null && user.getFollowedArtists().contains(targetId)) {
                    user.getFollowedArtists().remove(targetId);
                    userRepository.save(user);
                }
            });

            artistRepository.findById(targetId).ifPresent(artist -> {
                artist.setFollowers(Math.max(0, artist.getFollowers() - 1));
                artist.setFollowerCount(Math.max(0, artist.getFollowerCount() - 1));
                artistRepository.save(artist);
            });
        }
    }

    @Override
    public boolean isFollowing(String followerId, String targetId, String targetType) {
        try {
            Follow.TargetType type = Follow.TargetType.valueOf(targetType.toUpperCase());
            return followRepository.existsByFollowerIdAndTargetIdAndTargetType(followerId, targetId, type);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<FollowDto> getFollowers(String targetId, String targetType) {
        Follow.TargetType type = Follow.TargetType.valueOf(targetType.toUpperCase());
        List<Follow> follows = followRepository.findByTargetIdAndTargetType(targetId, type);
        return follows.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<FollowDto> getFollowing(String followerId, String targetType) {
        Follow.TargetType type = Follow.TargetType.valueOf(targetType.toUpperCase());
        List<Follow> follows = followRepository.findByFollowerIdAndTargetType(followerId, type);
        return follows.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public long getFollowerCount(String targetId, String targetType) {
        Follow.TargetType type = Follow.TargetType.valueOf(targetType.toUpperCase());
        return followRepository.countByTargetIdAndTargetType(targetId, type);
    }

    @Override
    public long getFollowingCount(String followerId, String targetType) {
        Follow.TargetType type = Follow.TargetType.valueOf(targetType.toUpperCase());
        return followRepository.countByFollowerIdAndTargetType(followerId, type);
    }

    private FollowDto convertToDto(Follow follow) {
        FollowDto dto = FollowDto.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .targetId(follow.getTargetId())
                .targetType(follow.getTargetType().name())
                .createdAt(follow.getCreatedAt())
                .build();

        // Nạp thông tin chi tiết người Follower
        userRepository.findById(follow.getFollowerId()).ifPresent(user -> {
            dto.setFollowerName(user.getUsername());
            dto.setFollowerAvatarUrl(user.getAvatarUrl());
        });

        // Nạp thông tin chi tiết đối tượng được Follow (Target)
        if (follow.getTargetType() == Follow.TargetType.USER) {
            userRepository.findById(follow.getTargetId()).ifPresent(user -> {
                dto.setTargetName(user.getUsername());
                dto.setTargetAvatarUrl(user.getAvatarUrl());
            });
        } else if (follow.getTargetType() == Follow.TargetType.ARTIST) {
            artistRepository.findById(follow.getTargetId()).ifPresent(artist -> {
                dto.setTargetName(artist.getName());
                dto.setTargetAvatarUrl(artist.getAvatarUrl());
            });
        }

        return dto;
    }
}