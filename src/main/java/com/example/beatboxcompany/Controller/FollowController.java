package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.FollowDto;
import com.example.beatboxcompany.Service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
// @CrossOrigin(origins = "*")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * Follow User hoặc Artist
     */
    @PostMapping
    public ResponseEntity<String> follow(
            @RequestParam String followerId,
            @RequestParam String targetId,
            @RequestParam String targetType
    ) {
        followService.follow(followerId, targetId, targetType);
        return ResponseEntity.ok("Follow successfully");
    }

    /**
     * Unfollow User hoặc Artist
     */
    @DeleteMapping
    public ResponseEntity<String> unfollow(
            @RequestParam String followerId,
            @RequestParam String targetId,
            @RequestParam String targetType
    ) {
        followService.unfollow(followerId, targetId, targetType);
        return ResponseEntity.ok("Unfollow successfully");
    }

    /**
     * Kiểm tra đã follow chưa
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> isFollowing(
            @RequestParam String followerId,
            @RequestParam String targetId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(
                followService.isFollowing(followerId, targetId, targetType)
        );
    }

    /**
     * Danh sách người theo dõi
     */
    @GetMapping("/followers")
    public ResponseEntity<List<FollowDto>> getFollowers(
            @RequestParam String targetId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(
                followService.getFollowers(targetId, targetType)
        );
    }

    /**
     * Danh sách đang follow
     */
    @GetMapping("/following")
    public ResponseEntity<List<FollowDto>> getFollowing(
            @RequestParam String followerId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(
                followService.getFollowing(followerId, targetType)
        );
    }

    /**
     * Số lượng followers
     */
    @GetMapping("/followers/count")
    public ResponseEntity<Long> getFollowerCount(
            @RequestParam String targetId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(
                followService.getFollowerCount(targetId, targetType)
        );
    }

    /**
     * Số lượng đang follow
     */
    @GetMapping("/following/count")
    public ResponseEntity<Long> getFollowingCount(
            @RequestParam String followerId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(
                followService.getFollowingCount(followerId, targetType)
        );
    }

}