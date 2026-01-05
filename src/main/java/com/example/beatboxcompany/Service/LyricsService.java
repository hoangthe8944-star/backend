package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.LyricsDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LyricsService {

    private final String LRCLIB_API_URL = "https://lrclib.net/api/get";
    private final RestTemplate restTemplate = new RestTemplate();

    public LyricsDto getLyrics(String track, String artist, String album, Integer durationSeconds) {
        // Xây dựng URL với các tham số
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(LRCLIB_API_URL)
                .queryParam("track_name", track)
                .queryParam("artist_name", artist)
                .queryParam("album_name", album)
                .queryParam("duration", durationSeconds);

        try {
            // Gọi API và map vào DTO
            return restTemplate.getForObject(builder.toUriString(), LyricsDto.class);
        } catch (Exception e) {
            // Nếu không tìm thấy (404) hoặc lỗi mạng, trả về null
            System.err.println("Lyrics not found or API error: " + e.getMessage());
            return null;
        }
    }
}