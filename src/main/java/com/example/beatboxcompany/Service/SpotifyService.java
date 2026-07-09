package com.example.beatboxcompany.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String searchUrl = "https://api.spotify.com/v1/search?q=" + encodedKeyword + "&type=track&limit=10";
            return callSpotifyApi(searchUrl, token);
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    // --- CÁC HÀM MỚI CẦN THÊM ĐỂ FIX LỖI ---

    /**
     * 3. Tìm kiếm Album
     */
    public Object searchAlbums(String keyword) {
        String token = getAccessToken();
        if (token == null)
            return Collections.singletonMap("error", "Lỗi Token");

        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String searchUrl = "https://api.spotify.com/v1/search?q=" + encodedKeyword + "&type=album&limit=10";
            return callSpotifyApi(searchUrl, token);
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
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
        } catch (HttpClientErrorException.TooManyRequests e) {
            HttpHeaders headersResponse = e.getResponseHeaders();
            String retryAfterStr = headersResponse != null ? headersResponse.getFirst("Retry-After") : null;
            int retryAfter = retryAfterStr != null ? Integer.parseInt(retryAfterStr) : 5;
            System.out.println("429 Too Many Requests on getArtistDetails. Retrying after " + retryAfter + " seconds...");
            try {
                Thread.sleep(retryAfter * 1000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return getArtistDetails(spotifyArtistId);
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

    private List<String> getFallbackGenres(String artistName) {
        String name = artistName.toLowerCase().trim();
        List<String> g = new ArrayList<>();

        if (name.contains("aespa") || name.contains("illit") || name.contains("jung kook") || name.contains("bts")
                || name.contains("blackpink") || name.contains("twice") || name.contains("newjeans")) {
            g.add("k-pop");
            g.add("pop");
            g.add("dance");
        } else if (name.contains("ariana grande") || name.contains("jessie j") || name.contains("doja cat")
                || name.contains("sza") || name.contains("taylor swift") || name.contains("bruno mars")
                || name.contains("billie eilish")) {
            g.add("pop");
            g.add("r&b");
            if (name.contains("doja cat"))
                g.add("rap");
            if (name.contains("jessie j") || name.contains("sza"))
                g.add("soul");
        } else if (name.contains("karik") || name.contains("low g") || name.contains("b ray") || name.contains("binz")
                || name.contains("24k.right") || name.contains("rap việt") || name.contains("hustlang")
                || name.contains("mck") || name.contains("double2t") || name.contains("suboi")
                || name.contains("tlinh")) {
            g.add("rap viet");
            g.add("hip-hop");
        } else if (name.contains("sơn tùng") || name.contains("jack") || name.contains("j97") || name.contains("only c")
                || name.contains("soobin") || name.contains("noo phước thịnh") || name.contains("quang hùng")
                || name.contains("52hz") || name.contains("ngô lan hương") || name.contains("icm")
                || name.contains("tvt") || name.contains("mono")) {
            g.add("v-pop");
            if (name.contains("soobin") || name.contains("52hz"))
                g.add("r&b");
            if (name.contains("ngô lan hương"))
                g.add("acoustic");
            if (name.contains("noo phước thịnh"))
                g.add("ballad");
            if (name.contains("sơn tùng") || name.contains("masterd") || name.contains("only c"))
                g.add("dance-pop");
        } else if (name.contains("thefatrat") || name.contains("marshmello") || name.contains("alan walker")
                || name.contains("martin garrix")) {
            g.add("edm");
            g.add("electro house");
        } else if (name.contains("worship") || name.contains("hillsong") || name.contains("elevation")) {
            g.add("christian");
            g.add("worship");
        } else if (name.contains("rap") || name.contains("hiphop") || name.contains("hip-hop") || name.contains("mc") || name.contains("dj")) {
            g.add("rap");
            g.add("hip-hop");
        } else if (name.contains("rock") || name.contains("band") || name.contains("metal") || name.contains("indie")) {
            g.add("rock");
            g.add("indie");
        } else if (name.contains("r&b") || name.contains("soul") || name.contains("rnb")) {
            g.add("r&b");
            g.add("soul");
        } else if (name.contains("dance") || name.contains("edm") || name.contains("remix") || name.contains("house")) {
            g.add("dance");
            g.add("edm");
        } else {
            g.add("pop");
        }

        if (isVietnameseName(artistName) && !g.contains("v-pop")) {
            g.add("v-pop");
        }

        return g;
    }

    private boolean isVietnameseName(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        String vnChars = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
        for (char c : vnChars.toCharArray()) {
            if (n.indexOf(c) >= 0) return true;
        }
        String[] vnWords = {"nguyen", "tran", "le", "pham", "hoang", "phan", "vu", "vo", "dang", "bui", "do", "ho", "ngo", "duong", "ly"};
        for (String w : vnWords) {
            if (n.contains(w)) return true;
        }
        return false;
    }

    private void mapFullDetailsToArtist(Map<String, Object> details, Artist artist) {
        System.out.println("===> Mapping Spotify Details for Artist: " + artist.getName());
        System.out.println("===> Details keys: " + details.keySet());

        // 1. Thể loại (Genres)
        if (details.containsKey("genres") && details.get("genres") != null
                && !((List<?>) details.get("genres")).isEmpty()) {
            List<String> spotifyGenres = (List<String>) details.get("genres");
            System.out.println("===> Found Spotify Genres: " + spotifyGenres);
            artist.setGenres(spotifyGenres);
        } else {
            List<String> fallback = getFallbackGenres(artist.getName());
            System.out.println("===> Spotify genres empty. Using fallback genres: " + fallback);
            artist.setGenres(fallback);
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
            artist.setFollowers(Long.valueOf(followers.get("total").toString()));
        }
        artist.setVerified(true);
        artist.setUpdatedAt(LocalDateTime.now());
    }

    private boolean isValidSpotifyId(String id) {
        return id != null && id.length() == 22 && id.matches("^[a-zA-Z0-9]+$");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getArtistsBatch(List<String> ids) {
        return getArtistsBatchWithRetry(ids, 0);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getArtistsBatchWithRetry(List<String> ids, int attempt) {
        String token = getAccessToken();

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Spotify Access Token is null");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < ids.size(); i += 20) {

            List<String> batch = ids.subList(i, Math.min(i + 20, ids.size()));

            String url = "https://api.spotify.com/v1/artists?ids="
                    + String.join(",", batch);

            System.out.println("\n========================================");
            System.out.println("Calling Spotify Artists API (Batch size: 20, Attempt: " + attempt + ")");
            System.out.println("URL      : " + url);
            System.out.println("Batch    : " + batch.size());
            System.out.println("IDs      : " + batch);
            System.out.println("========================================");

            boolean success = false;

            while (!success) {

                try {

                    java.net.URI uri = java.net.URI.create(url);
                    ResponseEntity<Map> response = restTemplate.exchange(
                             uri,
                             HttpMethod.GET,
                             entity,
                             Map.class);

                    System.out.println("Status : " + response.getStatusCode());

                    Map<String, Object> body = response.getBody();

                    if (body == null) {
                        System.out.println("Response Body = NULL");
                        break;
                    }

                    List<Map<String, Object>> artists = (List<Map<String, Object>>) body.get("artists");

                    if (artists != null) {

                        System.out.println("Artists Returned : " + artists.size());

                        for (Map<String, Object> artist : artists) {
                            if (artist != null) {
                                System.out.println("Artist : "
                                         + artist.get("id")
                                         + " | "
                                         + artist.get("name"));
                            }
                        }

                        result.addAll(artists);
                    }

                    success = true;

                }

                catch (HttpClientErrorException.TooManyRequests ex) {

                    System.out.println("\n========== 429 TOO MANY REQUESTS ==========");
                    System.out.println(ex.getResponseBodyAsString());

                    long wait = 3;

                    try {

                        String retry = ex.getResponseHeaders().getFirst("Retry-After");

                        if (retry != null)
                            wait = Long.parseLong(retry);

                    } catch (Exception ignored) {
                    }

                    System.out.println("Retry after " + wait + " sec");

                    try {
                        Thread.sleep(wait * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                }

                catch (HttpClientErrorException.Forbidden ex) {
                    System.out.println("\n========== 403 FORBIDDEN ==========");
                    System.out.println("Body: " + ex.getResponseBodyAsString());
                    if (attempt < 1) {
                        System.out.println("Token might be expired or invalid. Force refreshing token and retrying...");
                        this.accessToken = null; // Clear token cache
                        return getArtistsBatchWithRetry(ids, attempt + 1);
                    }
                    throw ex;
                }

                catch (HttpClientErrorException.Unauthorized ex) {
                    System.out.println("\n========== 401 UNAUTHORIZED ==========");
                    System.out.println("Body: " + ex.getResponseBodyAsString());
                    if (attempt < 1) {
                        System.out.println("Token is unauthorized. Force refreshing token and retrying...");
                        this.accessToken = null; // Clear token cache
                        return getArtistsBatchWithRetry(ids, attempt + 1);
                    }
                    throw ex;
                }

                catch (HttpClientErrorException ex) {

                    System.out.println("\n========== HTTP ERROR ==========");
                    System.out.println("Status : " + ex.getStatusCode());
                    System.out.println("Body:");
                    System.out.println(ex.getResponseBodyAsString());
                    throw ex;
                }

                catch (Exception ex) {

                    System.out.println("\n========== UNKNOWN ERROR ==========");
                    ex.printStackTrace();
                    throw new RuntimeException(ex);

                }

            }

            try {
                // Short break between sub-batches
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

        return result;
    }

    public int fixArtists() {

        List<Artist> artists = artistRepository.findAll();

        System.out.println("=== DB ARTISTS FORMAT DEBUG ===");
        artists.stream().limit(10).forEach(a -> 
            System.out.println("Artist Name: " + a.getName() + " | SpotifyId: '" + a.getSpotifyId() + "' | Genres: " + a.getGenres())
        );
        System.out.println("===============================");

        // Lấy artist có danh sách genres rỗng (null/empty) hoặc bị dính placeholder mặc định [pop, v-pop]
        List<Artist> emptyGenreArtists = artists.stream()
                .filter(a -> a.getGenres() == null 
                        || a.getGenres().isEmpty()
                        || (a.getGenres().contains("v-pop") && a.getGenres().contains("pop") && a.getGenres().size() <= 2))
                .toList();

        System.out.println("========================================");
        System.out.println("Starting batch fixArtists. Total needing genres update: " + emptyGenreArtists.size());
        System.out.println("========================================");

        // Group A: Cần tìm kiếm SpotifyId bằng tên (vì ID hiện tại là rỗng, null hoặc là MusicBrainz ID bắt đầu bằng "mb-")
        List<Artist> groupSearchByName = new ArrayList<>();
        // Group B: Đã có SpotifyId hợp lệ (22 ký tự base62), có thể gọi batch API trực tiếp
        List<Artist> groupBatchQuery = new ArrayList<>();

        for (Artist a : emptyGenreArtists) {
            String sId = a.getSpotifyId();
            if (sId == null || sId.isBlank() || sId.toLowerCase().startsWith("mb-") || !isValidSpotifyId(sId.trim())) {
                groupSearchByName.add(a);
            } else {
                groupBatchQuery.add(a);
            }
        }

        System.out.println("Group A (Requires name search because of invalid/MusicBrainz ID): " + groupSearchByName.size());
        System.out.println("Group B (Valid Spotify IDs for Batch lookup): " + groupBatchQuery.size());

        int updated = 0;

        // 1. Xử lý Group A: Tìm kiếm và đồng bộ ID thật từ Spotify bằng Tên (Giới hạn tối đa 5 nghệ sĩ mỗi lần gọi để tránh 429)
        int searchCount = 0;
        for (Artist artist : groupSearchByName) {
            if (searchCount >= 5) {
                System.out.println("Reached Group A search limit (5 artists) for this run. Rest will be processed in next runs to avoid 429.");
                break;
            }
            try {
                System.out.println("Searching and syncing real Spotify ID for: " + artist.getName());
                syncFullArtistData(artist);
                updated++;
                searchCount++;
                // Tránh lỗi 429
                Thread.sleep(1500);
            } catch (Exception e) {
                System.err.println("Failed to sync Group A artist: " + artist.getName() + " -> " + e.getMessage());
            }
        }

        // 2. Xử lý Group B: Gọi API theo batch 20
        int batchSize = 20;
        for (int i = 0; i < groupBatchQuery.size(); i += batchSize) {

            System.out.println("----------------------------------");
            System.out.println("Processing Group B batch " + (i / batchSize + 1) + " / " + ((groupBatchQuery.size() + batchSize - 1) / batchSize));

            List<Artist> batch = groupBatchQuery.subList(i,
                    Math.min(i + batchSize, groupBatchQuery.size()));

            List<String> ids = batch.stream()
                    .map(a -> a.getSpotifyId().trim())
                    .toList();

            System.out.println("Requesting batch IDs: " + ids);

            try {
                List<Map<String, Object>> spotifyArtists = new ArrayList<>();
                try {
                    spotifyArtists = getArtistsBatch(ids);
                } catch (Exception batchEx) {
                    System.out.println("Batch request failed: " + batchEx.getMessage() + ". Falling back to individual requests...");
                    for (String singleId : ids) {
                        try {
                            Map<String, Object> details = getArtistDetails(singleId);
                            if (details != null) {
                                spotifyArtists.add(details);
                            }
                            Thread.sleep(200);
                        } catch (Exception indEx) {
                            System.err.println("Failed to fetch individual details for ID: " + singleId + " -> " + indEx.getMessage());
                        }
                    }
                }

                Map<String, Map<String, Object>> spotifyMap = new HashMap<>();
                for (Map<String, Object> sp : spotifyArtists) {
                    if (sp != null) {
                        spotifyMap.put((String) sp.get("id"), sp);
                    }
                }

                for (Artist artist : batch) {
                    Map<String, Object> sp = spotifyMap.get(artist.getSpotifyId().trim());
                    if (sp == null) {
                        System.out.println("Spotify details not found for artist: " + artist.getName());
                        continue;
                    }

                    updateArtistFromSpotify(artist, sp);
                    artistRepository.save(artist);
                    updated++;
                    System.out.println("Updated successfully artist: " + artist.getName());
                }
            } catch (Exception e) {
                System.err.println("Failed to process batch starting at index " + i + ": " + e.getMessage());
            }

            try {
                System.out.println("Sleeping 1500ms to avoid 429 Rate Limit...");
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("========================================");
        System.out.println("Completed fixArtists. Total updated: " + updated);
        System.out.println("========================================");

        return updated;
    }


    private void updateArtistFromSpotify(
            Artist artist,
            Map<String, Object> spotifyArtist) {

        artist.setSpotifyId(
                (String) spotifyArtist.get("id"));

        Number popularity = (Number) spotifyArtist.get("popularity");

        if (popularity != null) {

            artist.setPopularity(
                    popularity.intValue());

        }

        Map<String, Object> external = (Map<String, Object>) spotifyArtist.get("external_urls");

        if (external != null) {

            artist.setSpotifyUrl(
                    (String) external.get("spotify"));

        }

        List<String> genres = (List<String>) spotifyArtist.get("genres");

        if (genres != null && !genres.isEmpty()) {

            artist.setGenres(genres);

        } else {

            artist.setGenres(
                    getFallbackGenres(artist.getName()));

        }

        Map<String, Object> followers = (Map<String, Object>) spotifyArtist.get("followers");

        if (followers != null) {

            Number total = (Number) followers.get("total");

            artist.setFollowers(total.longValue());

            artist.setFollowerCount(total.longValue());

        }

        List<Map<String, Object>> images = (List<Map<String, Object>>) spotifyArtist.get("images");

        if (images != null && !images.isEmpty()) {

            List<String> urls = new ArrayList<>();

            for (Map<String, Object> img : images) {

                urls.add((String) img.get("url"));

            }

            artist.setImages(urls);

            artist.setAvatarUrl(urls.get(0));

            artist.setImageUrl(urls.get(0));

            artist.setCoverImageUrl(urls.get(0));

        }

        artist.setVerified(true);

        artist.setUpdatedAt(LocalDateTime.now());

    }
}
