package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.CommentDto;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Repository.SongRepository;
import com.example.beatboxcompany.Service.AlbumService;
import com.example.beatboxcompany.Service.ArtistService;
import com.example.beatboxcompany.Service.CommentService;
import com.example.beatboxcompany.Service.PlaylistService;
import com.example.beatboxcompany.Service.SongService; // Import Interface

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.http.HttpStatus;
import java.io.IOException;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final SongService songService;
    private final AlbumService albumService;
    private final PlaylistService playlistService;
    private final CommentService commentService;
    private final SongRepository songRepository;
    private final ArtistService artistService;

    // Đường dẫn file nhạc để stream (Giữ lại theo yêu cầu)
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public PublicController(
            SongService songService,
            AlbumService albumService,
            PlaylistService playlistService,
            SongRepository songRepository,
            CommentService commentService,
            ArtistService artistService) {
        this.songService = songService;
        this.albumService = albumService;
        this.playlistService = playlistService;
        this.commentService = commentService;
        this.songRepository = songRepository;
        this.artistService = artistService;
    }

    // --- 1. Tìm kiếm và Trending ---

    /**
     * [ĐÃ CHỈNH SỬA]
     * API Tìm kiếm công khai.
     * Sử dụng logic tìm kiếm thông minh mới: ưu tiên tìm nghệ sĩ trước.
<<<<<<< HEAD
     */
    @GetMapping("/search")
    public ResponseEntity<List<SongDto>> search(@RequestParam("q") String query) {
        // THAY ĐỔI DUY NHẤT: Gọi đến hàm `searchPublicSongs` thay vì `searchSongs`
=======
     * Hỗ trợ lọc theo thể loại (category) nếu có categoryId.
     */
    @GetMapping("/search")
    public ResponseEntity<List<SongDto>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "categoryId", required = false) String categoryId) {

        // Nếu có categoryId, gọi hàm search có lọc
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            return ResponseEntity.ok(songService.searchPublicSongs(query, categoryId));
        }

        // Nếu không có categoryId, gọi hàm search thông thường
>>>>>>> c4ff2b6 (Mới chỉnh lọc Search theo mụ)
        return ResponseEntity.ok(songService.searchPublicSongs(query));
    }

    @GetMapping("/songs/trending")
    public ResponseEntity<List<SongDto>> getTrendingSongs(@RequestParam(defaultValue = "10") int limit) {
        List<SongDto> trending = songService.getTrendingPublishedSongs(limit);
        return ResponseEntity.ok(trending);
    }

    // --- 2. Truy cập Album, Playlist (Giữ nguyên) ---

    @GetMapping("/albums/{albumId}")
    public ResponseEntity<AlbumDto> getAlbumDetails(@PathVariable String albumId) {
        // Giả định AlbumService đã có hàm này
        // return ResponseEntity.ok(albumService.getPublishedAlbumById(albumId));
        return ResponseEntity.ok(null); // Tạm thời null để code không lỗi biên dịch
    }

    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<PlaylistDto> getPublicPlaylist(@PathVariable String playlistId) {
        // Giả định PlaylistService đã có hàm này
        // return
        // ResponseEntity.ok(playlistService.getPublicPlaylistDetails(playlistId));
        return ResponseEntity.ok(null); // Tạm thời null
    }

    // --- 3. Streaming và Tương tác (Giữ nguyên) ---

    @GetMapping("/songs/{songId}/info")
    public ResponseEntity<SongDto> getSongInfo(@PathVariable String songId) {
        SongDto song = songService.getPublishedSongById(songId);
        songService.incrementViewCount(songId); // Tăng view
        return ResponseEntity.ok(song);
    }

    /**
     * API Streaming file nhạc từ server cục bộ.
     * (Giữ lại theo yêu cầu)
     */
    @GetMapping("/songs/stream/{fileName:.+}")
    public ResponseEntity<Resource> streamSongFile(
            @PathVariable String fileName,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            long contentLength = resource.contentLength();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));

            if (rangeHeader == null) {
                headers.setContentLength(contentLength);
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            }

            try {
                List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
                if (ranges.size() == 1) {
                    HttpRange range = ranges.get(0);
                    long start = range.getRangeStart(contentLength);
                    long end = range.getRangeEnd(contentLength);
                    long rangeLength = end - start + 1;

                    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
                    headers.setContentLength(rangeLength);
                    headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength);

                    return new ResponseEntity<>(resource, headers, HttpStatus.PARTIAL_CONTENT);
                }
            } catch (IllegalArgumentException e) {
                headers.setContentLength(contentLength);
                return new ResponseEntity<>(resource, headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
            }

            headers.setContentLength(contentLength);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/songs/{songId}/comments")
    public ResponseEntity<List<CommentDto>> getSongComments(@PathVariable String songId) {
        songService.getPublishedSongById(songId); // Check tồn tại
        // return ResponseEntity.ok(commentService.getCommentsBySongId(songId));
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/songs/all")
    public ResponseEntity<List<SongDto>> getAllPublicSongs() {
        // Lấy tất cả bài hát có status là "PUBLISHED"
        return ResponseEntity.ok(songService.getSongsByStatus("PUBLISHED"));
    }
<<<<<<< HEAD
=======

>>>>>>> c4ff2b6 (Mới chỉnh lọc Search theo mụ)
    @GetMapping("/songs/category/{categoryId}")
    public ResponseEntity<List<Song>> getSongsByCategory(@PathVariable String categoryId) {
        List<Song> songs = songRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(songs);
    }
<<<<<<< HEAD
     @GetMapping("/artists")
=======

    @GetMapping("/artists")
>>>>>>> c4ff2b6 (Mới chỉnh lọc Search theo mụ)
    public ResponseEntity<List<ArtistDto>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    // Lấy chi tiết nghệ sĩ cho trang ArtistPage
    @GetMapping("/artists/{id}")
    public ResponseEntity<ArtistDto> getArtistById(@PathVariable String id) {
        return ResponseEntity.ok(artistService.getPublicArtistProfile(id));
    }

    // Lấy nghệ sĩ theo thể loại cho trang Category
    @GetMapping("/artists/genre/{genre}")
    public ResponseEntity<List<ArtistDto>> getArtistsByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(artistService.getArtistsByGenre(genre));
    }
<<<<<<< HEAD
=======

>>>>>>> c4ff2b6 (Mới chỉnh lọc Search theo mụ)
    // Lấy TẤT CẢ bài hát của một nghệ sĩ
    // GET http://localhost:8081/api/public/songs/artist/{artistId}
    @GetMapping("/songs/artists/{artistId}")
    public ResponseEntity<List<SongDto>> getSongsByArtist(@PathVariable String artistId) {
        return ResponseEntity.ok(artistService.getSongsByArtistId(artistId));
    }

    // Lấy TẤT CẢ album của một nghệ sĩ
    // GET http://localhost:8081/api/public/albums/artist/{artistId}
    @GetMapping("/albums/artists/{artistId}")
    public ResponseEntity<List<AlbumDto>> getAlbumsByArtist(@PathVariable String artistId) {
        return ResponseEntity.ok(artistService.getAlbumsByArtistId(artistId));
    }
}