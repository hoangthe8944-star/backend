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

    public LyricsDto getLyrics(String track, String artist) {
        // Bước 1: Thử gọi API "Get" (Khớp chính xác) - Bỏ Album để tăng tỉ lệ trúng
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://lrclib.net/api/get")
                    .queryParam("track_name", track)
                    .queryParam("artist_name", artist)
                    .toUriString();

            return restTemplate.getForObject(url, LyricsDto.class);
        } catch (Exception e) {
            // Bước 2: Nếu Get thất bại (404), chuyển sang "Search" (Tìm kiếm gần đúng)
            System.out.println("Exact match failed, switching to search for: " + track);
            return searchFallback(track, artist);
        }
    }

    private LyricsDto searchFallback(String track, String artist) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://lrclib.net/api/search")
                    .queryParam("q", track + " " + artist)
                    .build().toUriString();

            // API Search của LRCLIB trả về mảng [], phải dùng LyricsDto[]
            LyricsDto[] results = restTemplate.getForObject(url, LyricsDto[].class);

            if (results != null && results.length > 0) {
                return results[0]; // Lấy bài hát đầu tiên tìm thấy
            }
        } catch (Exception e) {
            System.err.println("Search also failed: " + e.getMessage());
        }
        return null;
    }
}