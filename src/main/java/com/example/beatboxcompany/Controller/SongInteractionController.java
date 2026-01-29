package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs") // Endpoint chung
public class SongInteractionController {

    private final SongService songService;

    public SongInteractionController(SongService songService) {
        this.songService = songService;
    }

    /**
     * API để ghi nhận một bài hát đã được phát.
     * Frontend sẽ gọi API này mỗi khi một bài hát bắt đầu phát.
     * Yêu cầu xác thực.
     */
    @PostMapping("/{songId}/playback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> recordPlayback(@PathVariable String songId) {
        songService.recordSongPlayback(songId);
        // Trả về 200 OK không cần nội dung
        return ResponseEntity.ok().build();
    }

    /**
     * API để lấy danh sách bài hát đã phát gần đây của người dùng.
     * Yêu cầu xác thực.
     */
    @GetMapping("/history/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SongDto>> getRecentHistory(@RequestParam(defaultValue = "20") int limit) {
        List<SongDto> songs = songService.getRecentlyPlayedSongs(limit);
        return ResponseEntity.ok(songs);
    }
}