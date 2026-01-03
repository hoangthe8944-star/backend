package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lyrics")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsService lyricsService;

    @GetMapping("/{spotifyId}")
    public LyricsDto getLyrics(@PathVariable String spotifyId) {
        LyricsDto lyrics = lyricsService.getLyricsBySpotifyTrackId(spotifyId);
        if (lyrics == null) {
            return new LyricsDto(); // trả về empty lyrics thay vì 500
        }
        return lyrics;
    }
}
