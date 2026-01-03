package com.example.beatboxcompany.Service.Impl;

import java.util.Collections;

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
    public LyricsDto getLyricsBySpotifyId(String spotifyId) {
        // 1️⃣ thử LRCLIB trước (sync lyrics)
        LyricsDto lrc = tryLrcLib(spotifyId);
        if (lrc != null) return lrc;

        // 2️⃣ fallback lyricstify
        return tryLyricstify(spotifyId);
    }

    // ===============================
    // LRCLIB (hay lỗi TLS → nuốt lỗi)
    // ===============================
    private LyricsDto tryLrcLib(String spotifyId) {
        try {
            String url = "https://lrclib.net/api/get?spotify_id=" + spotifyId;
            return restTemplate.getForObject(url, LyricsDto.class);
        } catch (Exception e) {
            System.err.println("LRCLIB ignored: " + e.getMessage());
            return null;
        }
    }

    // ===============================
    // LYRICSTIFY (ổn định hơn)
    // ===============================
    private LyricsDto tryLyricstify(String spotifyId) {
        try {
            String url = "https://api.lyricstify.vercel.app/api/lyrics/" + spotifyId;

            LyricsDto dto = restTemplate.getForObject(url, LyricsDto.class);
            if (dto == null) dto = new LyricsDto();

            dto.setSource("lyricstify");
            dto.setLines(Collections.emptyList()); // KHÔNG có timestamp
            return dto;

        } catch (Exception e) {
            // ❌ KHÔNG throw → FE không bao giờ 500
            LyricsDto empty = new LyricsDto();
            empty.setLyrics("");
            empty.setLines(Collections.emptyList());
            empty.setSource("none");
            return empty;
        }
    }
}
