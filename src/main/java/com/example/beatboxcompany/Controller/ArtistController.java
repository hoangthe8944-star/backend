package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Request.AlbumRequest;
import com.example.beatboxcompany.Service.AlbumService;
import com.example.beatboxcompany.Service.ArtistService;
import com.example.beatboxcompany.Service.SongService; // Import Interface
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artist")
@PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
public class ArtistController {

    // SỬA: Luôn khai báo Interface, không khai báo Class Impl
    private final SongService songService;
    private final ArtistService artistService;
    private final AlbumService albumService;

    public ArtistController(SongService songService, ArtistService artistService, AlbumService albumService) {
        this.songService = songService;
        this.artistService = artistService;
        this.albumService = albumService;
    }

    // --- Helper: Lấy Email của User đang đăng nhập ---
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Trả về email (username) từ Token
    }

    // --- Quản lý Bài hát (Songs) ---

    /**
     * POST /api/artist/songs/upload
     * Nghệ sĩ upload bài hát mới (Trạng thái sẽ là PENDING)
     */
    @PostMapping(value = "/songs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSong(
            @RequestParam("title") String title,
            @RequestParam(value = "albumId", required = false) String albumId, // Có thể null nếu là Single
            @RequestParam("coverUrl") String coverUrl,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam("audioFile") MultipartFile audioFile) {

        try {
            String userEmail = getCurrentUserEmail();
            
            // Gọi Service xử lý (Lưu file, lưu DB với trạng thái PENDING)
            SongDto newSong = songService.createPendingSong(title, albumId, coverUrl, duration, audioFile, userEmail);
            
            return new ResponseEntity<>(newSong, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }

    /**
     * GET /api/artist/songs/my-uploads
     * Xem danh sách bài hát mình đã đăng
     */
    @GetMapping("/songs/my-uploads")
    public ResponseEntity<List<SongDto>> getMyUploads() {
        String userEmail = getCurrentUserEmail();
        List<SongDto> mySongs = songService.getSongsByUploader(userEmail);
        return ResponseEntity.ok(mySongs);
    }

    // --- Quản lý Album ---

    @PostMapping("/albums/create")
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody AlbumRequest request) {
        String userEmail = getCurrentUserEmail();
        // Logic tạo Album chờ duyệt
        AlbumDto newAlbum = albumService.createPendingAlbum(request, userEmail);
        return new ResponseEntity<>(newAlbum, HttpStatus.CREATED);
    }

    @GetMapping("/albums/my-uploads")
    public ResponseEntity<List<AlbumDto>> getMyAlbums() {
        String userEmail = getCurrentUserEmail();
        List<AlbumDto> myAlbums = albumService.getAlbumsByUploader(userEmail);
        return ResponseEntity.ok(myAlbums);
    }
}