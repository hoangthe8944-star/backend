package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.CommentDto;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Request.CommentRequest;
import com.example.beatboxcompany.Request.PlaylistRequest;

import com.example.beatboxcompany.Service.CommentService;
import com.example.beatboxcompany.Service.PlaylistService;
import com.example.beatboxcompany.Service.SongService; // Import Interface
import com.example.beatboxcompany.Service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
public class UserController {

    private final UserService userService;
    private final PlaylistService playlistService;
    private final CommentService commentService;
    private final SongService songService; // SỬA: Dùng Interface
    private final UserRepository userRepository;

    public UserController(
            UserService userService,
            PlaylistService playlistService,
            CommentService commentService,
            UserRepository userRepository,
            SongService songService) { // SỬA: Inject Interface
        this.userService = userService;
        this.playlistService = playlistService;
        this.commentService = commentService;
        this.songService = songService;
        this.userRepository = userRepository;
    }

    /**
     * Helper: Lấy ID của user đang đăng nhập từ Token
     * Lưu ý: JWT thường lưu Email làm Subject. Cần tìm ID từ Email.
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Gọi UserService để tìm ID từ Email (Bạn cần đảm bảo UserService có hàm này)
        // Nếu UserService trả về DTO, hãy lấy .getId(). Nếu trả về Entity, cũng lấy
        // .getId()
        return userService.getUserIdByEmail(email);
    }

    // --- 1. Quản lý Hồ sơ & Tương tác ---

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        String userId = getCurrentUserId();
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/songs/{songId}/like")
    public ResponseEntity<Void> toggleLikeSong(@PathVariable String songId) {
        String userId = getCurrentUserId();
        // userService.toggleLike(userId, songId); // Cần implement trong UserService
        return ResponseEntity.ok().build();
    }

    // --- 2. Quản lý Playlist ---

    @PostMapping("/playlists")
    public ResponseEntity<PlaylistDto> createPlaylist(@RequestBody PlaylistRequest request) {
        String userId = getCurrentUserId();
        PlaylistDto newPlaylist = playlistService.createPlaylist(request, userId);
        return new ResponseEntity<>(newPlaylist, HttpStatus.CREATED);
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists() {
        String userId = getCurrentUserId();
        List<PlaylistDto> playlists = playlistService.getUserPlaylists(userId);
        return ResponseEntity.ok(playlists);
    }

    @PostMapping("/playlists/{playlistId}/tracks/{trackId}")
    public ResponseEntity<PlaylistDto> addTrack(@PathVariable String playlistId, @PathVariable String trackId) {
        String userId = getCurrentUserId();
        PlaylistDto updatedPlaylist = playlistService.addTrackToPlaylist(playlistId, trackId, userId);
        return ResponseEntity.ok(updatedPlaylist);
    }

    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable String playlistId) {
        String userId = getCurrentUserId();
        playlistService.deletePlaylist(playlistId, userId);
        return ResponseEntity.noContent().build();
    }

    // --- 3. Quản lý Bình luận ---

    @PostMapping("/songs/{songId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable String songId,
            @RequestBody CommentRequest request) {

        String userId = getCurrentUserId();
        CommentDto newComment = commentService.createComment(songId, request, userId);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteMyComment(@PathVariable String commentId) {
        String userId = getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String searchKey = auth.getName(); // Đây là giá trị lấy từ Token

            System.out.println("===> USER CONTROLLER DEBUG: Đang tìm User với Key: [" + searchKey + "]");

            // Tìm thử theo Email
            Optional<User> userOpt = userRepository.findByEmail(searchKey);

            // Nếu không thấy, hãy thử tìm theo Username (đề phòng getName() trả về
            // username)
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(searchKey);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("username", user.getUsername());
                userInfo.put("roles", user.getAuthorities());
                userInfo.put("isVerified", user.isVerified());
                return ResponseEntity.ok(userInfo);
            } else {
                System.err.println("===> USER CONTROLLER DEBUG: Không tìm thấy bất kỳ User nào khớp với: " + searchKey);
                return ResponseEntity.status(404).body("Không tìm thấy thông tin người dùng trong hệ thống.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
        }
    }

}