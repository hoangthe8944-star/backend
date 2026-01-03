package com.example.beatboxcompany.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lyrics")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsService lyricsService;

    @GetMapping("/{songId}")
    public ResponseEntity<?> getLyrics(@PathVariable String songId) {
        try {
            return ResponseEntity.ok(lyricsService.getLyricsBySongId(songId));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Không thể tải lyrics"));
        }
    }
}
