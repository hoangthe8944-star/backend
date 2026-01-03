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
        if (spotifyId == null) return null;

        String url = "https://api.lyricstify.vercel.app/api/lyrics/" + spotifyId;
        return restTemplate.getForObject(url, LyricsDto.class);
    }
}
