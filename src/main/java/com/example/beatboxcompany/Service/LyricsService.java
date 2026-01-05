package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.LyricsDto;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LyricsService {
    private final RestTemplate restTemplate = new RestTemplate();

    public LyricsDto getLyrics(String track, String artist, String album, Integer durationSeconds) {
        // Bước 1: Thử gọi API "Get" (Khớp chính xác) - Bỏ Album để tăng tỉ lệ trúng
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://lrclib.net/api/get")
                    .queryParam("track_name", track)
                    .queryParam("artist_name", artist)
                    .toUriString();
            
            return restTemplate.getForObject(url, LyricsDto.class);
        } catch (Exception e) {
            // Bước 2: Nếu Get thất bại (404), chuyển sang "Search" (Tìm kiếm gần đúng)
            return searchFallback(track, artist);
        }
    }

    private LyricsDto searchFallback(String track, String artist) {
        try {
            // Chỉ tìm theo tên bài + nghệ sĩ, bỏ qua album phức tạp
            String query = track + " " + artist;
            String url = UriComponentsBuilder.fromHttpUrl("https://lrclib.net/api/search")
                    .queryParam("q", query)
                    .toUriString();

            List<Map<String, Object>> results = restTemplate.getForObject(url, List.class);
            if (results != null && !results.isEmpty()) {
                Map<String, Object> first = results.get(0);
                LyricsDto dto = new LyricsDto();
                dto.setPlainLyrics((String) first.get("plainLyrics"));
                dto.setSyncedLyrics((String) first.get("syncedLyrics"));
                return dto;
            }
        } catch (Exception e) {
            System.out.println("Search failed too");
        }
        return null;
    }
}