package com.example.beatboxcompany.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")

    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Autowired
    private ArtistRepository artistRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken = null;
    private long tokenExpirationTime = 0;

    // --- 1. Quản lý Token ---
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
            System.err.println("Lỗi Token: " + e.getMessage());
        }
        return null;
    }

    // 2. Tìm kiếm Bài hát (Giữ nguyên)
    public Object searchTracks(String keyword) {
        String token = getAccessToken();
        if (token == null)
            return Collections.singletonMap("error", "Lỗi Token");

        String searchUrl = "https://api.spotify.com/v1/search?q=" + keyword + "&type=track&limit=10";
        return callSpotifyApi(searchUrl, token);
    }

    // --- CÁC HÀM MỚI CẦN THÊM ĐỂ FIX LỖI ---

    /**
     * 3. Tìm kiếm Album
     */
    public Object searchAlbums(String keyword) {
        String token = getAccessToken();
        if (token == null)
            return Collections.singletonMap("error", "Lỗi Token");

        String searchUrl = "https://api.spotify.com/v1/search?q=" + keyword + "&type=album&limit=10";
        return callSpotifyApi(searchUrl, token);
    }

    /**
     * 4. Lấy chi tiết Album (Bao gồm danh sách bài hát bên trong)
     * Hàm này đang bị thiếu gây ra lỗi undefined
     */
    public Map<String, Object> getAlbumDetails(String spotifyAlbumId) {
        String token = getAccessToken();
        if (token == null)
            throw new RuntimeException("Không lấy được Token Spotify");

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

    public Map<String, Object> getArtistDetails(String spotifyArtistId) {
        String token = getAccessToken();
        if (token == null)
            throw new RuntimeException("Không lấy được Token Spotify");

        String url = "https://api.spotify.com/v1/artists/" + spotifyArtistId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Spotify API lấy Artist: " + e.getMessage());
        }
    }

    /**
     * 6. Logic Ánh xạ (Mapping) Thể loại
     * Spotify trả về hàng ngàn genre (ví dụ: "vietnamese hip hop", "v-pop")
     * Hàm này sẽ quy chúng về các Category chính của bạn.
     */
    public String mapSpotifyGenreToCategory(List<String> spotifyGenres) {
        if (spotifyGenres == null || spotifyGenres.isEmpty())
            return "others";

        // Chuyển tất cả về chữ thường để so sánh
        String allGenres = String.join(" ", spotifyGenres).toLowerCase();

        if (allGenres.contains("pop") || allGenres.contains("v-pop"))
            return "pop";
        if (allGenres.contains("hip hop") || allGenres.contains("rap") || allGenres.contains("trap"))
            return "hip-hop";
        if (allGenres.contains("rock") || allGenres.contains("metal") || allGenres.contains("indie"))
            return "rock";
        if (allGenres.contains("edm") || allGenres.contains("electro") || allGenres.contains("dance"))
            return "edm";
        if (allGenres.contains("r&b") || allGenres.contains("rnb") || allGenres.contains("soul"))
            return "rnb";
        if (allGenres.contains("jazz") || allGenres.contains("blues"))
            return "jazz";
        if (allGenres.contains("chill") || allGenres.contains("acoustic") || allGenres.contains("lo-fi"))
            return "chill";
        if (allGenres.contains("classical") || allGenres.contains("piano"))
            return "classical";

        return "others";
    }

    /**
     * 7. Hàm tổng hợp: Lấy thể loại chính của một Nghệ sĩ
     */
    public String getMainCategoryOfArtist(String spotifyArtistId) {
        Map<String, Object> artistDetails = getArtistDetails(spotifyArtistId);
        if (artistDetails != null && artistDetails.containsKey("genres")) {
            List<String> spotifyGenres = (List<String>) artistDetails.get("genres");
            return mapSpotifyGenreToCategory(spotifyGenres);
        }
        return "others";
    }

    public void syncFullArtistData(Artist artist) {
        String token = getAccessToken();
        try {
            // PHẢI MÃ HÓA TÊN (Vì Karik hay Ariana Grande có thể chứa ký tự lạ)
            String encodedName = java.net.URLEncoder.encode(artist.getName(), "UTF-8");

            // 1. Tìm ID thật của Spotify bằng Tên
            String searchUrl = "https://api.spotify.com/v1/search?q=" + encodedName + "&type=artist&limit=1";
            Map<String, Object> searchResponse = (Map<String, Object>) callSpotifyApi(searchUrl, token);

            // Trích xuất ID thật (Ví dụ Karik -> 49asC8pS9Ix7YvSfc8vj9H)
            Map<String, Object> artistsMap = (Map<String, Object>) searchResponse.get("artists");
            List<Map<String, Object>> items = (List<Map<String, Object>>) artistsMap.get("items");

            if (items != null && !items.isEmpty()) {
                String realSpotifyId = (String) items.get(0).get("id");

                // 2. Lấy Full Profile từ ID thật này
                Map<String, Object> details = getArtistDetails(realSpotifyId);

                // 3. Đổ dữ liệu vào Entity artist
                mapFullDetailsToArtist(details, artist);

                // 4. LƯU XUỐNG MONGODB (Cực kỳ quan trọng)
                artistRepository.save(artist);
            }
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ: " + e.getMessage());
        }
    }

    private void mapFullDetailsToArtist(Map<String, Object> details, Artist artist) {
        // 1. Thể loại (Genres)
        if (details.containsKey("genres")) {
            artist.setGenres((List<String>) details.get("genres"));
        }
        // 2. Hình ảnh (Images)
        if (details.containsKey("images")) {
            List<Map<String, Object>> images = (List<Map<String, Object>>) details.get("images");
            if (!images.isEmpty()) {
                List<String> urls = new ArrayList<>();
                for (Map<String, Object> img : images)
                    urls.add((String) img.get("url"));
                artist.setImages(urls);
                artist.setAvatarUrl(urls.get(0)); // Ảnh chất lượng cao nhất
                artist.setCoverImageUrl(urls.get(0));
            }
        }
        // 3. Followers
        if (details.containsKey("followers")) {
            Map<String, Object> followers = (Map<String, Object>) details.get("followers");
            artist.setFollowerCount(Long.valueOf(followers.get("total").toString()));
        }
        artist.setVerified(true);
        artist.setUpdatedAt(LocalDateTime.now());
    }

    // --- 3. Helper Method ---
}