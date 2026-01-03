package com.example.beatboxcompany.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin
public class LyricsController {

    private final LyricsService lyricsService;

@GetMapping("/{spotifyId}")
    public LyricsDto getLyrics(@PathVariable String spotifyId) {
        return lyricsService.getLyricsBySpotifyId(spotifyId);
    }
}
