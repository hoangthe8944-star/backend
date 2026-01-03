package com.example.beatboxcompany.Service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LyricsServiceImpl implements LyricsService {

    private final RestTemplate restTemplate;

    @Override
    public LyricsDto getLyricsBySpotifyTrackId(String spotifyId) {
        if (spotifyId == null || spotifyId.isEmpty()) return new LyricsDto();

        // Lyricstify
        String url = "https://api.lyricstify.vercel.app/api/lyrics/" + spotifyId;
        try {
            return restTemplate.getForObject(url, LyricsDto.class);
        } catch (Exception e) {
            System.err.println("Lyricstify error: " + e.getMessage());
        }

        // Optional: fallback LRC (text only, không timestamp)
        try {
            String lrcUrl = "https://lrclib.net/api/get?artist=&track_id=" + spotifyId;
            LyricsDto fallback = restTemplate.getForObject(lrcUrl, LyricsDto.class);
            if (fallback != null && fallback.getLyrics() != null)
                return fallback;
        } catch (Exception e) {
            System.err.println("LRC fallback error: " + e.getMessage());
        }

        // Không có lyrics
        return new LyricsDto();
    }
}
