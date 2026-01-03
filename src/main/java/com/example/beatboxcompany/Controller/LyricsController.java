package com.example.beatboxcompany.Controller;

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

    @GetMapping("/{spotifyTrackId}")
    public ResponseEntity<LyricsDto> getLyrics(
            @PathVariable String spotifyTrackId
    ) {
        return ResponseEntity.ok(
            lyricsService.getLyricsBySpotifyTrackId(spotifyTrackId)
        );
    }
}
