package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;
import com.example.beatboxcompany.Service.PlaylistService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
// @CrossOrigin(origins = "*") // Mở cái này nếu bạn gặp lỗi CORS khi gọi từ
// GitHub Pages
public class PlaylistController {

    private final PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // ----- Tạo playlist -----
    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @RequestBody PlaylistRequest request,
            // Chấp nhận cả viết hoa lẫn viết thường bằng cách dùng Alias
            @RequestHeader(value = "currentuserid", required = false) String currentUserId,
            @RequestHeader(value = "isadmin", defaultValue = "false") boolean isAdmin) {
        // Dùng System.err để log hiện ra màu nổi bật hơn trong bảng log Render
        System.err.println("=====> BACKEND NHẬN ĐƯỢC ID LÀ: " + currentUserId);

        PlaylistDto dto = playlistService.createPlaylist(request, currentUserId, isAdmin);
        return ResponseEntity.ok(dto);
    }

    // ----- [MỚI] Lấy playlist của chính người đang đăng nhập -----
    // Giúp Frontend chỉ cần gọi /api/playlists/me là xong
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistDto>> getMyPlaylists(
            @RequestHeader("currentUserId") String currentUserId) {
        List<PlaylistDto> playlists = playlistService.getUserPlaylists(currentUserId);
        return ResponseEntity.ok(playlists);
    }

    // ----- Cập nhật playlist -----
    @PutMapping("/{playlistId}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable String playlistId,
            @RequestBody PlaylistRequest request,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {
        try {
            PlaylistDto dto = playlistService.updatePlaylist(playlistId, request, currentUserId, isAdmin);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ----- Lấy tất cả playlist của user bất kỳ (ví dụ trang cá nhân người khác)
    // -----
    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(@PathVariable String ownerId) {
        List<PlaylistDto> playlists = playlistService.getUserPlaylists(ownerId);
        return ResponseEntity.ok(playlists);
    }

    // ----- Lấy chi tiết playlist public (Dùng cho trang PlaylistDetail) -----
    @GetMapping("/public/{playlistId}")
    public ResponseEntity<PlaylistDto> getPublicPlaylistDetails(@PathVariable String playlistId) {
        PlaylistDto dto = playlistService.getPublicPlaylistDetails(playlistId);
        return ResponseEntity.ok(dto);
    }

    // ----- Lấy danh sách tất cả playlist public (Dùng cho trang chủ) -----
    @GetMapping("/public")
    public ResponseEntity<List<PlaylistDto>> getPublicPlaylists() {
        List<PlaylistDto> playlists = playlistService.getPublicPlaylists();
        return ResponseEntity.ok(playlists);
    }

    // ----- Thêm/Xóa bài hát và Xóa Playlist (Giữ nguyên hoặc thêm try-catch
    // SecurityException) -----
    @PostMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> addTrackToPlaylist(
            @PathVariable String playlistId,
            @PathVariable String trackId,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {
        try {
            PlaylistDto dto = playlistService.addTrackToPlaylist(playlistId, trackId, currentUserId, isAdmin);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String trackId,
            @RequestHeader("currentUserId") String currentUserId) {
        try {
            PlaylistDto dto = playlistService.removeTrackFromPlaylist(playlistId, trackId, currentUserId);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<?> deletePlaylist(
            @PathVariable String playlistId,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin) {
        try {
            playlistService.deletePlaylist(playlistId, currentUserId, isAdmin);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}