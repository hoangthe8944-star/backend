package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Dto.LyricsLine;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Repository.SongRepository;
import com.example.beatboxcompany.Service.LyricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LyricsServiceImpl implements LyricsService {

    private final RestTemplate restTemplate;
    private final SongRepository songRepository;

    @Override
    public LyricsDto getLyricsBySongId(String songId) {

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // 1️⃣ Thử LRC trước (timestamp)
        LyricsDto lrc = tryLrc(song);
        if (lrc != null) {
            lrc.setSynced(true);
            return lrc;
        }

        // 2️⃣ Fallback Lyricstify
        LyricsDto plain = tryLyricstify(song.getSpotifyId());
        plain.setSynced(false);
        return plain;
    }

    // ===== LRCLIB =====
    private LyricsDto tryLrc(Song song) {
        try {
            String url = "https://lrclib.net/api/get?"
                    + "track_name=" + URLEncoder.encode(song.getTitle(), StandardCharsets.UTF_8)
                    + "&artist_name=" + URLEncoder.encode(song.getArtistName(), StandardCharsets.UTF_8);

            Map res = restTemplate.getForObject(url, Map.class);
            if (res == null || res.get("syncedLyrics") == null) return null;

            return parseLrc((String) res.get("syncedLyrics"));

        } catch (Exception e) {
            return null;
        }
    }

    // ===== LYRICSTIFY =====
    private LyricsDto tryLyricstify(String spotifyId) {
        LyricsDto dto = new LyricsDto();
        List<LyricsLine> lines = new ArrayList<>();

        if (spotifyId == null) {
            dto.setLines(List.of(new LyricsLine(0.0, "Không có lời bài hát")));
            return dto;
        }

        String url = "https://api.lyricstify.vercel.app/api/lyrics/" + spotifyId;
        Map res = restTemplate.getForObject(url, Map.class);

        String lyrics = (String) res.get("lyrics");
        for (String line : lyrics.split("\n")) {
            lines.add(new LyricsLine(null, line));
        }

        dto.setLines(lines);
        return dto;
    }

    // ===== LRC PARSER =====
    private LyricsDto parseLrc(String lrc) {
        List<LyricsLine> lines = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+\\.\\d+)](.+)");
        for (String line : lrc.split("\n")) {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                double time = Integer.parseInt(m.group(1)) * 60
                        + Double.parseDouble(m.group(2));
                lines.add(new LyricsLine(time, m.group(3).trim()));
            }
        }

        LyricsDto dto = new LyricsDto();
        dto.setLines(lines);
        return dto;
    }
}
