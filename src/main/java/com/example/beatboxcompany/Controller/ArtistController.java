package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Request.AlbumRequest;
import com.example.beatboxcompany.Service.AlbumService;
import com.example.beatboxcompany.Service.ArtistService;
import com.example.beatboxcompany.Service.SongService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artist")
// Chỉ những người có Role ARTIST hoặc ADMIN mới được phép truy cập vào các tài nguyên quản lý này
@PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
public class ArtistController {

    private final SongService songService;
    private final ArtistService artistService;
    private final AlbumService albumService;

    public ArtistController(SongService songService, ArtistService artistService, AlbumService albumService) {
        this.songService = songService;
        this.artistService = artistService;
        this.albumService = albumService;
    }

    // ====================================================
    // 1. QUẢN LÝ HỒ SƠ (Profile Management)
    // ====================================================

    /**
     * Lấy hồ sơ của chính tôi (Dành cho Artist tự xem)
     */
    @GetMapping("/me")
    public ResponseEntity<ArtistDto> getMyProfile(@RequestHeader("currentUserId") String userId) {
        ArtistDto profile = artistService.getCurrentArtistProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Cập nhật hồ sơ của chính tôi (Bio, Ảnh bìa, Genres/Thể loại...)
     */
    @PutMapping("/me")
    public ResponseEntity<ArtistDto> updateMyProfile(
            @RequestHeader("currentUserId") String userId,
            @RequestBody ArtistDto updateDto) {
        ArtistDto updated = artistService.updateArtistProfile(userId, updateDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * ADMIN tạo hồ sơ Nghệ sĩ cho một User
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistDto> createArtistByAdmin(
            @RequestParam String userId,
            @RequestParam String artistName) {
        ArtistDto newArtist = artistService.createArtistByAdmin(userId, artistName);
        return new ResponseEntity<>(newArtist, HttpStatus.CREATED);
    }

    /**
     * ADMIN xem danh sách tất cả nghệ sĩ trong hệ thống
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArtistDto>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    // ====================================================
    // 2. QUẢN LÝ BÀI HÁT (Songs Management)
    // ====================================================

    /**
     * Nghệ sĩ Upload bài hát mới (Chờ duyệt)
     */
    @PostMapping(value = "/songs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSong(
            @RequestParam("title") String title,
            @RequestParam(value = "albumId", required = false) String albumId,
            @RequestParam("coverUrl") String coverUrl,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestHeader("currentUserId") String userId) {
        try {
            SongDto newSong = songService.createPendingSong(title, albumId, coverUrl, duration, audioFile, userId);
            return new ResponseEntity<>(newSong, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách bài hát do chính tôi đã upload
     */
    @GetMapping("/songs/my-uploads")
    public ResponseEntity<List<SongDto>> getMySongs(@RequestHeader("currentUserId") String userId) {
        List<SongDto> mySongs = songService.getSongsByUploader(userId);
        return ResponseEntity.ok(mySongs);
    }

    // ====================================================
    // 3. QUẢN LÝ ALBUM (Albums Management)
    // ====================================================

    /**
     * Nghệ sĩ tạo Album mới
     */
    @PostMapping("/albums/create")
    public ResponseEntity<?> createAlbum(
            @RequestBody AlbumRequest request,
            @RequestHeader("currentUserId") String userId) {
        try {
            AlbumDto newAlbum = albumService.createPendingAlbum(request, userId);
            return new ResponseEntity<>(newAlbum, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo album: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách Album của chính tôi
     */
    @GetMapping("/albums/my-uploads")
    public ResponseEntity<List<AlbumDto>> getMyAlbums(@RequestHeader("currentUserId") String userId) {
        List<AlbumDto> myAlbums = albumService.getAlbumsByUploader(userId);
        return ResponseEntity.ok(myAlbums);
    }
}