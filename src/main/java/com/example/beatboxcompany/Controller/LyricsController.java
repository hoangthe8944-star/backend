package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lyrics")
public class LyricsController {

    @Autowired
    private LyricsService lyricsService;

    @GetMapping
    public ResponseEntity<LyricsDto> getLyrics(
            @RequestParam String track,
            @RequestParam String artist,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer duration) {

        LyricsDto lyrics = lyricsService.getLyrics(track, artist, album, duration);

        if (lyrics != null) {
            return ResponseEntity.ok(lyrics);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}