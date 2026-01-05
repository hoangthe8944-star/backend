package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lyrics") // Phải khớp 100% với Frontend gọi
@CrossOrigin(origins = "*")
public class LyricsController {

    @Autowired
    private LyricsService lyricsService;

    @GetMapping
    public ResponseEntity<?> getLyrics(
            @RequestParam String track,
            @RequestParam String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer duration) {

        LyricsDto result = lyricsService.getLyrics(track, artist, album, duration);

        // LUÔN trả về OK (200), nếu không có lời thì trả về object rỗng
        if (result == null) {
            return ResponseEntity.ok(Map.of("message", "No lyrics found"));
        }
        return ResponseEntity.ok(result);
    }
}