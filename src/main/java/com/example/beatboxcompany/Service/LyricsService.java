package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.LyricsDto;

public interface LyricsService {
    // LyricsDto getLyricsBySpotifyTrackId(String spotifyId);
    LyricsDto getLyricsBySongId(String songId);
}
