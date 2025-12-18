package com.example.beatboxcompany.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken = null;
    private long tokenExpirationTime = 0;

    // 1. Lấy Token (Giữ nguyên)
    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return accessToken;
        }

        String authUrl = "https://accounts.spotify.com/api/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                this.accessToken = (String) responseBody.get("access_token");
                int expiresIn = (int) responseBody.get("expires_in");
                this.tokenExpirationTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
                return this.accessToken;
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy Token Spotify: " + e.getMessage());
        }
        return null;
    }

    // 2. Tìm kiếm Bài hát (Giữ nguyên)
    public Object searchTracks(String keyword) {
        String token = getAccessToken();
        if (token == null) return Collections.singletonMap("error", "Lỗi Token");

        String searchUrl = "https://api.spotify.com/v1/search?q=" + keyword + "&type=track&limit=10";
        return callSpotifyApi(searchUrl, token);
    }

    // --- CÁC HÀM MỚI CẦN THÊM ĐỂ FIX LỖI ---

    /**
     * 3. Tìm kiếm Album
     */
    public Object searchAlbums(String keyword) {
        String token = getAccessToken();
        if (token == null) return Collections.singletonMap("error", "Lỗi Token");

        String searchUrl = "https://api.spotify.com/v1/search?q=" + keyword + "&type=album&limit=10";
        return callSpotifyApi(searchUrl, token);
    }

    /**
     * 4. Lấy chi tiết Album (Bao gồm danh sách bài hát bên trong)
     * Hàm này đang bị thiếu gây ra lỗi undefined
     */
    public Map<String, Object> getAlbumDetails(String spotifyAlbumId) {
        String token = getAccessToken();
        if (token == null) throw new RuntimeException("Không lấy được Token Spotify");

        String url = "https://api.spotify.com/v1/albums/" + spotifyAlbumId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Gọi API lấy chi tiết album
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Spotify API lấy Album: " + e.getMessage());
        }
    }

    // Helper method để gọi API chung
    private Object callSpotifyApi(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}