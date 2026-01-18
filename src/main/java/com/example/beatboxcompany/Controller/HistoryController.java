package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.HistoryDto;
import com.example.beatboxcompany.Service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // 1. Lưu lượt nghe (Gọi khi bài hát bắt đầu phát)
    @PostMapping("/{songId}")
    public ResponseEntity<Void> recordPlay(
            @PathVariable String songId,
            @RequestHeader("currentUserId") String userId) {
        historyService.recordSongPlay(userId, songId);
        return ResponseEntity.ok().build();
    }

    // 2. Lấy danh sách đã nghe gần đây (Trang Recently Played)
    @GetMapping("/me")
    public ResponseEntity<List<HistoryDto>> getMyHistory(
            @RequestHeader("currentUserId") String userId) {
        List<HistoryDto> history = historyService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }

    // 3. Xóa lịch sử
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearMyHistory(
            @RequestHeader("currentUserId") String userId) {
        historyService.clearHistory(userId);
        return ResponseEntity.noContent().build();
    }
}