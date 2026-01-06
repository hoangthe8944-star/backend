package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;
import com.example.beatboxcompany.Service.PlaylistService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // ----- Tạo playlist -----
    @PostMapping
    public ResponseEntity<PlaylistDto> createPlaylist(
            @RequestBody PlaylistRequest request,
            @RequestHeader(value = "currentUserId", required = false) String currentUserId, 
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin
    ) {
        PlaylistDto dto = playlistService.createPlaylist(request, currentUserId, isAdmin);
        return ResponseEntity.ok(dto);
    }

    // ----- Cập nhật playlist -----
    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> updatePlaylist(
            @PathVariable String playlistId,
            @RequestBody PlaylistRequest request,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin
    ) {
        PlaylistDto dto = playlistService.updatePlaylist(playlistId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(dto);
    }

    // ----- Lấy tất cả playlist của user -----
    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<PlaylistDto>> getUserPlaylists(@PathVariable String ownerId) {
        List<PlaylistDto> playlists = playlistService.getUserPlaylists(ownerId);
        return ResponseEntity.ok(playlists);
    }

    // ----- Lấy chi tiết playlist public -----
    @GetMapping("/public/{playlistId}")
    public ResponseEntity<PlaylistDto> getPublicPlaylistDetails(@PathVariable String playlistId) {
        PlaylistDto dto = playlistService.getPublicPlaylistDetails(playlistId);
        return ResponseEntity.ok(dto);
    }

    // ----- Lấy danh sách tất cả playlist public -----
    @GetMapping("/public")
    public ResponseEntity<List<PlaylistDto>> getPublicPlaylists() {
        List<PlaylistDto> playlists = playlistService.getPublicPlaylists();
        return ResponseEntity.ok(playlists);
    }

    // ----- Thêm track vào playlist -----
    @PostMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<PlaylistDto> addTrackToPlaylist(
            @PathVariable String playlistId,
            @PathVariable String trackId,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin
    ) {
        PlaylistDto dto = playlistService.addTrackToPlaylist(playlistId, trackId, currentUserId, isAdmin);
        return ResponseEntity.ok(dto);
    }

    // ----- Xóa track khỏi playlist -----
    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<PlaylistDto> removeTrackFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String trackId,
            @RequestHeader("currentUserId") String currentUserId
    ) {
        PlaylistDto dto = playlistService.removeTrackFromPlaylist(playlistId, trackId, currentUserId);
        return ResponseEntity.ok(dto);
    }

    // ----- Xóa playlist -----
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable String playlistId,
            @RequestHeader("currentUserId") String currentUserId,
            @RequestHeader(value = "isAdmin", defaultValue = "false") boolean isAdmin
    ) {
        playlistService.deletePlaylist(playlistId, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
