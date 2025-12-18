package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Dto.SubscriptionDto;
import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SongService songService;
    private final UserService userService;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SubscriptionService subscriptionService;
    private final SpotifyService spotifyService;

    public AdminController(
            SongService songService,
            UserService userService,
            ArtistService artistService,
            AlbumService albumService,
            SubscriptionService subscriptionService,
            SpotifyService spotifyService) {
        this.songService = songService;
        this.userService = userService;
        this.artistService = artistService;
        this.albumService = albumService;
        this.subscriptionService = subscriptionService;
        this.spotifyService = spotifyService;
    }

    // -----------------------------------------------------
    // --- 1. TÍCH HỢP SPOTIFY & IMPORT BÀI HÁT ---
    // -----------------------------------------------------

    @GetMapping("/songs")
    public ResponseEntity<List<SongDto>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @GetMapping("/spotify/search")
    public ResponseEntity<Object> searchSpotify(@RequestParam String keyword) {
        return ResponseEntity.ok(spotifyService.searchTracks(keyword));
    }

    @GetMapping("/spotify/search-albums")
    public ResponseEntity<Object> searchSpotifyAlbums(@RequestParam String keyword) {
        return ResponseEntity.ok(spotifyService.searchAlbums(keyword));
    }

    @PostMapping("/albums/import-spotify")
    public ResponseEntity<?> importSpotifyAlbum(@RequestParam String spotifyId) {
        try {
            // Gọi Service để xử lý logic
            albumService.importAlbumFromSpotify(spotifyId);
            return ResponseEntity.ok("Import Album và danh sách bài hát thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug nếu cần
            return ResponseEntity.badRequest().body("Lỗi import: " + e.getMessage());
        }
    }

    @PostMapping(value = "/songs/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSong(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("album") String album,
            @RequestParam("coverUrl") String coverUrl,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam(value = "spotifyId", required = false) String spotifyId,
            @RequestParam("audioFile") MultipartFile audioFile) {

        try {
            songService.createSong(title, artist, album, coverUrl, duration, spotifyId, audioFile);
            return ResponseEntity.ok("Import bài hát thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // -----------------------------------------------------
    // --- 2. QUẢN LÝ BÀI HÁT (SONGS) ---
    // -----------------------------------------------------

    @PostMapping("/songs/{id}/upload-file")
    public ResponseEntity<?> uploadSongFile(
            @PathVariable("id") String songId,
            @RequestParam("file") MultipartFile file) {

        try {
            songService.uploadSongFile(songId, file);
            return ResponseEntity.ok("Upload file thành công!");
        } catch (Exception e) {
            // DÒNG QUAN TRỌNG NHẤT – IN RA MESSAGE THẬT
            e.printStackTrace(); // ← THÊM DÒNG NÀY
            System.err.println("LỖI UPLOAD FILE: " + e.getMessage());

            return ResponseEntity.badRequest()
                    .body("Upload thất bại: " + e.getMessage()); // ← trả message rõ ràng
        }
    }

    /**
     * POST /api/admin/songs/bulk-import
     * Upload file ZIP chứa nhiều bài hát
     */
    @PostMapping(value = "/songs/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkImportSongs(@RequestParam("zipFile") MultipartFile zipFile) {
        if (!zipFile.getOriginalFilename().endsWith(".zip")) {
            return ResponseEntity.badRequest().body("Vui lòng upload file .zip");
        }

        try {
            songService.bulkImportFromZip(zipFile);
            return ResponseEntity.ok("Đã import hàng loạt thành công! Kiểm tra danh sách bài hát.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi import: " + e.getMessage());
        }
    }

    @GetMapping("/songs/pending")
    public ResponseEntity<List<SongDto>> getPendingSongs() {
        return ResponseEntity.ok(songService.getSongsByStatus("PENDING"));
    }

    @PutMapping("/songs/{songId}/approve")
    public ResponseEntity<SongDto> approveSong(@PathVariable String songId) {
        SongDto approvedSong = songService.updateStatus(songId, "PUBLISHED");
        return ResponseEntity.ok(approvedSong);
    }

    // -----------------------------------------------------
    // --- 3. QUẢN LÝ NGƯỜI DÙNG (USERS) ---
    // -----------------------------------------------------

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------
    // --- 4. QUẢN LÝ GÓI THÀNH VIÊN ---
    // -----------------------------------------------------

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<SubscriptionDto> createSubscription(@RequestBody SubscriptionDto subscriptionDto) {
        SubscriptionDto newSubscription = subscriptionService.createSubscription(subscriptionDto);
        return ResponseEntity.ok(newSubscription);
    }

    @DeleteMapping("/subscriptions/{subId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable String subId) {
        subscriptionService.deleteSubscription(subId);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------
    // --- 5. QUẢN LÝ ALBUM (ALBUMS) ---
    // -----------------------------------------------------

    /**
     * [MỚI BỔ SUNG] API lấy tất cả Album (Fix lỗi 403/404)
     */
    @GetMapping("/albums")
    public ResponseEntity<List<AlbumDto>> getAllAlbums() {
        // Đảm bảo AlbumService đã có hàm getAllAlbums() như hướng dẫn trước
        return ResponseEntity.ok(albumService.getAllAlbums());
    }

    @GetMapping("/albums/pending")
    public ResponseEntity<List<AlbumDto>> getPendingAlbums() {
        return ResponseEntity.ok(albumService.getAlbumsByStatus("PENDING"));
    }

    @PutMapping("/albums/{albumId}/status")
    public ResponseEntity<AlbumDto> updateAlbumStatus(
            @PathVariable String albumId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        AlbumDto updatedAlbum = albumService.updateAlbumStatus(albumId, status, reason);
        return ResponseEntity.ok(updatedAlbum);
    }

    // -----------------------------------------------------
    // --- 6. QUẢN LÝ NGHỆ SĨ (ARTISTS) ---
    // -----------------------------------------------------

    @GetMapping("/artists")
    public ResponseEntity<List<ArtistDto>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @PutMapping("/artists/{artistId}/name")
    public ResponseEntity<ArtistDto> updateArtistName(@PathVariable String artistId, @RequestParam String newName) {
        ArtistDto updatedArtist = artistService.adminUpdateName(artistId, newName);
        return ResponseEntity.ok(updatedArtist);
    }

    @PostMapping("/artists/create")
    public ResponseEntity<?> createArtistByAdmin(@RequestBody CreateArtistRequest request) {
        try {
            ArtistDto artist = artistService.createArtistByAdmin(request.userId(), request.artistName());
            return ResponseEntity.ok(artist);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    record CreateArtistRequest(String userId, String artistName) {
    }
}