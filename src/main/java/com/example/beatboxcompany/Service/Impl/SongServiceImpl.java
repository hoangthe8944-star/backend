package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Dto.UploadResultDto;
import com.example.beatboxcompany.Entity.Album;
import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Repository.AlbumRepository;
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Repository.SongRepository;
import com.example.beatboxcompany.Service.CloudinaryService;
import com.example.beatboxcompany.Service.SongService;
import com.example.beatboxcompany.Service.SpotifyService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SpotifyService spotifyService;
    private final Environment environment;
    private final CloudinaryService cloudinaryService;

    public SongServiceImpl(SongRepository songRepository, ArtistRepository artistRepository,
            AlbumRepository albumRepository, SpotifyService spotifyService, Environment environment,
            CloudinaryService cloudinaryService) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.spotifyService = spotifyService;
        this.environment = environment;
        this.cloudinaryService = cloudinaryService;
    }

    // ... (Các phương thức createSong, deleteSong, createPendingSong,
    // uploadSongFile, bulkImportFromZip giữ nguyên)
    // ... (Chúng đã được viết tốt và không cần thay đổi)
    @Override
    @Transactional
    public Song createSong(String title, String artistName, String albumName,
            String coverUrl, Integer durationSec, String spotifyId,
            MultipartFile audioFile) {

        // 1. UPLOAD FILE LÊN CLOUDINARY
        if (audioFile == null || audioFile.isEmpty()) {
            throw new RuntimeException("File âm thanh không được để trống.");
        }
        UploadResultDto uploadResult = cloudinaryService.uploadFile(audioFile, "audio");

        // 2. Xử lý Nghệ sĩ (Artist)
        Artist artist = artistRepository.findByName(artistName)
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(artistName);
                    newArtist.setUserId("admin_import_" + UUID.randomUUID().toString());
                    newArtist.setVerified(true);
                    newArtist.setCreatedAt(LocalDateTime.now());
                    newArtist.setFollowerCount(0);
                    return artistRepository.save(newArtist);
                });

        // 3. Xử lý Album
        Album album = albumRepository.findByTitleAndArtistId(albumName, artist.getId())
                .orElseGet(() -> {
                    Album newAlbum = new Album();
                    newAlbum.setTitle(albumName);
                    newAlbum.setArtistId(artist.getId());
                    newAlbum.setCoverUrl(coverUrl);
                    newAlbum.setStatus("PUBLISHED");
                    newAlbum.setTotalTracks(0);
                    newAlbum.setTotalDurationMs(0L);
                    newAlbum.setCreatedAt(LocalDateTime.now());
                    newAlbum.setUpdatedAt(LocalDateTime.now());
                    return albumRepository.save(newAlbum);
                });

        // 4. Lưu Song với thông tin từ Cloudinary
        Song song = new Song();
        song.setTitle(title);
        song.setArtistId(artist.getId());
        song.setAlbumId(album.getId());
        song.setDuration(durationSec);
        song.setDurationMs(durationSec * 1000L);
        song.setCoverUrl(coverUrl);
        song.setSpotifyId(spotifyId);
        song.setStreamUrl(uploadResult.getSecureUrl());
        song.setStreamPublicId(uploadResult.getPublicId());
        song.setFilePath(null); // Không dùng trường này nữa
        song.setStatus("PUBLISHED");
        song.setViewCount(0L);
        song.setIsExplicit(false);
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName(artist.getName());

        Song savedSong = songRepository.save(song);

        // 5. Cập nhật Album
        if (album.getTrackIds() != null) {
            album.getTrackIds().add(savedSong.getId());
            long currentTotalDuration = album.getTotalDurationMs() != null ? album.getTotalDurationMs() : 0L;
            album.setTotalDurationMs(currentTotalDuration + savedSong.getDurationMs());
            album.setTotalTracks(album.getTrackIds().size());
            albumRepository.save(album);
        }

        return savedSong;
    }

    @Override
    @Transactional
    public void deleteSong(String id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát với ID: " + id));

        // Xóa file trên Cloudinary nếu có publicId
        if (song.getStreamPublicId() != null && !song.getStreamPublicId().isEmpty()) {
            cloudinaryService.deleteFile(song.getStreamPublicId());
        }

        // Xóa bài hát khỏi MongoDB
        songRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SongDto createPendingSong(String title, String albumId, String coverUrl,
            Integer duration, MultipartFile audioFile, String userEmail) {
        // 1. (Logic tìm nghệ sĩ của bạn)
        // Artist artist = artistRepository.findByUserEmail(userEmail)
        // .orElseThrow(() -> new RuntimeException("Bạn chưa tạo hồ sơ Nghệ sĩ!"));

        // 2. UPLOAD FILE LÊN CLOUDINARY
        if (audioFile == null || audioFile.isEmpty()) {
            throw new RuntimeException("File âm thanh không được để trống.");
        }
        // Lưu vào thư mục riêng cho các bài hát chờ duyệt
        UploadResultDto uploadResult = cloudinaryService.uploadFile(audioFile, "audio_pending");

        // 3. LƯU VÀO DATABASE
        Song song = new Song();
        song.setTitle(title);
        // song.setArtistId(artist.getId());
        if (albumId != null) {
            song.setAlbumId(albumId);
        }
        song.setCoverUrl(coverUrl);
        song.setDuration(duration);
        song.setStreamUrl(uploadResult.getSecureUrl());
        song.setStreamPublicId(uploadResult.getPublicId());
        song.setFilePath(null); // Không dùng file cục bộ
        song.setStatus("PENDING"); // Trạng thái chờ duyệt
        song.setCreatedAt(LocalDateTime.now());

        Song savedSong = songRepository.save(song);
        return convertToDto(savedSong);
    }

    @Override
    @Transactional
    public void uploadSongFile(String songId, MultipartFile file) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát"));

        if (!"PENDING".equals(song.getStatus())) {
            throw new RuntimeException("Chỉ có thể upload file cho bài hát có trạng thái PENDING");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File không được rỗng");
        }

        // 1. Nếu bài hát đã có file cũ, hãy xóa nó đi để tránh rác
        if (song.getStreamPublicId() != null && !song.getStreamPublicId().isEmpty()) {
            cloudinaryService.deleteFile(song.getStreamPublicId());
        }

        // 2. Upload file mới lên Cloudinary
        UploadResultDto uploadResult = cloudinaryService.uploadFile(file, "audio");

        // 3. Cập nhật thông tin và trạng thái cho bài hát
        song.setStreamUrl(uploadResult.getSecureUrl());
        song.setStreamPublicId(uploadResult.getPublicId());
        song.setFilePath(null);
        song.setStatus("PUBLISHED");

        songRepository.save(song);
    }

    @Override
    @Transactional
    public void bulkImportFromZip(MultipartFile zipFile) {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().toLowerCase().endsWith(".mp3")) {
                    processSingleZipEntry(zis, zipEntry.getName());
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi giải nén file ZIP: " + e.getMessage());
        }
    }

    // ... các import và các phương thức khác giữ nguyên

    private void processSingleZipEntry(ZipInputStream zis, String entryName) throws IOException {
        // Phần 1: Tách title và artist từ tên file - giữ nguyên
        File fileObj = new File(entryName);
        String originalFileName = fileObj.getName();
        String nameWithoutExt = originalFileName.replaceAll("(?i)\\.mp3$", "");
        String titleSearch = nameWithoutExt;
        String artistSearch = "";
        String[] separators = { "-", "–", "—", "\\|", ":", "·", "\\.", "\\(", "\\)", "feat", "ft", "with" };
        for (String sep : separators) {
            if (nameWithoutExt.contains(sep)) {
                String[] parts = nameWithoutExt.split(sep, 2);
                artistSearch = parts[0].trim();
                titleSearch = parts[1].trim();
                break;
            }
        }
        if (artistSearch.isEmpty()) {
            titleSearch = nameWithoutExt.trim();
        }
        titleSearch = cleanTitle(titleSearch);

        // Phần 2: Tìm metadata trên Spotify - giữ nguyên logic, chỉ lấy thông tin
        String finalTitle = titleSearch;
        String finalArtist = artistSearch.isEmpty() ? "Unknown Artist" : artistSearch;
        String finalAlbum = "Single";
        String coverUrl = "";
        String spotifyId = null;
        Integer durationSec = 0;
        try {
            String query = (artistSearch.isEmpty() ? "" : artistSearch + " ") + titleSearch;
            Object searchResult = spotifyService.searchTracks(query.trim());
            if (searchResult instanceof Map) {
                Map<String, Object> result = (Map<String, Object>) searchResult;
                Map<String, Object> tracks = (Map<String, Object>) result.get("tracks");
                if (tracks != null) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) tracks.get("items");
                    if (items != null && !items.isEmpty()) {
                        Map<String, Object> track = items.get(0);
                        finalTitle = (String) track.get("name");
                        spotifyId = (String) track.get("id");
                        durationSec = ((Integer) track.get("duration_ms")) / 1000;
                        List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
                        if (artists != null && !artists.isEmpty())
                            finalArtist = (String) artists.get(0).get("name");
                        Map<String, Object> album = (Map<String, Object>) track.get("album");
                        if (album != null) {
                            finalAlbum = (String) album.get("name");
                            List<Map<String, Object>> images = (List<Map<String, Object>>) album.get("images");
                            if (images != null && !images.isEmpty())
                                coverUrl = (String) images.get(0).get("url");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tìm Spotify cho file: " + originalFileName + " | " + e.getMessage());
        }

        // =========================================================================
        // [THAY ĐỔI] KIỂM TRA DỮ LIỆU TRÙNG LẶP TRƯỚC KHI UPLOAD VÀ LƯU
        // =========================================================================
        if (songRepository.existsByTitleIgnoreCaseAndArtistNameIgnoreCase(finalTitle, finalArtist)) {
            System.out.println("Bỏ qua bài hát trùng lặp: '" + finalTitle + "' - '" + finalArtist + "'");
            return; // Dừng xử lý file này và chuyển sang file tiếp theo trong ZIP
        }

        // Phần 3: Đọc file từ ZIP stream vào byte array (giữ nguyên)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zis.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        byte[] fileBytes = baos.toByteArray();

        // Phần 4: Upload file lên Cloudinary (giữ nguyên)
        UploadResultDto uploadResult = cloudinaryService.uploadFileBytes(fileBytes, "audio", "audio/mpeg");

        // Phần 5: Lưu vào DB (giữ nguyên)
        saveSongToDb(finalTitle, finalArtist, finalAlbum, coverUrl, durationSec, spotifyId,
                uploadResult.getSecureUrl(), uploadResult.getPublicId());
    }

    // ... các phương thức còn lại không đổi
    private void saveSongToDb(String title, String artistName, String albumName, String coverUrl, Integer duration,
            String spotifyId, String streamUrl, String streamPublicId) {
        // Tìm hoặc tạo Artist
        Artist artist = artistRepository.findByName(artistName)
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(artistName);
                    newArtist.setUserId("bulk_" + UUID.randomUUID().toString());
                    newArtist.setVerified(true);
                    newArtist.setCreatedAt(LocalDateTime.now());
                    newArtist.setFollowerCount(0);
                    return artistRepository.save(newArtist);
                });

        // Tìm hoặc tạo Album
        Album album = albumRepository.findByTitleAndArtistId(albumName, artist.getId())
                .orElseGet(() -> {
                    Album newAlbum = new Album();
                    newAlbum.setTitle(albumName);
                    newAlbum.setArtistId(artist.getId());
                    newAlbum.setCoverUrl(coverUrl);
                    newAlbum.setStatus("PUBLISHED");
                    newAlbum.setCreatedAt(LocalDateTime.now());
                    newAlbum.setUpdatedAt(LocalDateTime.now());
                    newAlbum.setTrackIds(new ArrayList<>());
                    return albumRepository.save(newAlbum);
                });

        // Lưu Song
        Song song = new Song();
        song.setTitle(title);
        song.setArtistId(artist.getId());
        song.setAlbumId(album.getId());
        song.setDuration(duration);
        song.setDurationMs(duration * 1000L);
        song.setCoverUrl(coverUrl);
        song.setStreamUrl(streamUrl);
        song.setStreamPublicId(streamPublicId);
        song.setFilePath(null);
        song.setSpotifyId(spotifyId);
        song.setStatus("PUBLISHED");
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName(artist.getName());
        Song savedSong = songRepository.save(song);

        // Update Album
        album.getTrackIds().add(savedSong.getId());
        album.setTotalTracks(album.getTrackIds().size());
        albumRepository.save(album);
    }
    // ======================================================================
    // CÁC PHƯƠNG THỨC KHÁC
    // ======================================================================

    @Override
    public List<SongDto> getSongsByStatus(String status) {
        return songRepository.findByStatus(status).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public SongDto updateStatus(String songId, String newStatus) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát: " + songId));
        song.setStatus(newStatus);
        return convertToDto(songRepository.save(song));
    }

    @Override
    public List<SongDto> getAllSongs() {
        return songRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * [ĐÃ SỬA LỖI]
     * Phương thức tìm kiếm cho trang Admin.
     * Sử dụng phương thức tìm kiếm rộng mới thay vì phương thức cũ đã bị xóa.
     */
    @Override
    public List<SongDto> searchSongs(String keyword) {
        // Gọi phương thức tìm kiếm rộng, truyền keyword vào cả 2 tham số.
        return songRepository.findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SongDto getPublishedSongById(String id) {
        Song song = songRepository.findById(id).orElseThrow(() -> new RuntimeException("Song not found"));
        return convertToDto(song);
    }

    @Override
    public void incrementViewCount(String id) {
        Song song = songRepository.findById(id).orElse(null);
        if (song != null) {
            song.setViewCount(song.getViewCount() + 1);
            songRepository.save(song);
        }
    }

    @Override
    public List<SongDto> getTrendingPublishedSongs(int limit) {
        return songRepository.findByStatus("PUBLISHED").stream()
                .sorted((s1, s2) -> Long.compare(s2.getViewCount(), s1.getViewCount()))
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDto> getSongsByUploader(String userId) {
        return List.of();
    }

    /**
     * [ĐÃ TRIỂN KHAI]
     * Phương thức tìm kiếm thông minh cho người dùng công khai.
     * Ưu tiên tìm nghệ sĩ trước, nếu không thấy mới tìm kiếm rộng.
     */
    @Override
    public List<SongDto> searchPublicSongs(String query) {
        // Bước 1: Thử tìm nghệ sĩ có tên khớp (không phân biệt hoa thường)
        Optional<Artist> artistOptional = artistRepository.findByNameIgnoreCase(query);

        // Bước 2: Nếu tìm thấy, trả về tất cả bài hát của họ
        if (artistOptional.isPresent()) {
            Artist artist = artistOptional.get();
            return songRepository.findByArtistId(artist.getId()).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        // Bước 3: Nếu không tìm thấy nghệ sĩ, thực hiện tìm kiếm rộng
        return songRepository.findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(query, query)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordSongPlayback(String songId) {
        // Tìm bài hát trong database
        songRepository.findById(songId).ifPresent(song -> {
            // Cập nhật thời gian nghe cuối cùng là thời điểm hiện tại
            song.setLastPlayedAt(LocalDateTime.now());
            // Lưu lại thay đổi
            songRepository.save(song);
        });
        // Nếu không tìm thấy bài hát, chúng ta có thể bỏ qua một cách lặng lẽ
        // hoặc ghi log nếu cần thiết.
    }

    /**
     * [TRIỂN KHAI]
     * Lấy danh sách các bài hát đã được phát gần đây.
     * Đây là một hành động "đọc", không cần @Transactional.
     */
    @Override
    public List<SongDto> getRecentlyPlayedSongs(int limit) {
        // Gọi phương thức repository đã tạo ở Bước 2
        List<Song> recentlyPlayedSongs = songRepository
                .findByStatusAndLastPlayedAtIsNotNullOrderByLastPlayedAtDesc("PUBLISHED");

        // Giới hạn số lượng kết quả và chuyển đổi sang DTO
        return recentlyPlayedSongs.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- MAPPER & HELPER ---
    private SongDto convertToDto(Song song) {
        if (song == null)
            return null;

        SongDto dto = new SongDto();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setCoverUrl(song.getCoverUrl());
        dto.setDuration(song.getDuration());
        dto.setStatus(song.getStatus());
        dto.setViewCount(song.getViewCount());
        dto.setIsExplicit(song.getIsExplicit());
        dto.setGenre(song.getGenre());
        dto.setStreamUrl(song.getStreamUrl());

        if (song.getArtistId() != null) {
            artistRepository.findById(song.getArtistId())
                    .ifPresentOrElse(artist -> dto.setArtistName(artist.getName()),
                            () -> dto.setArtistName("Unknown Artist"));
        } else {
            dto.setArtistName("Unknown Artist");
        }

        if (song.getAlbumId() != null) {
            albumRepository.findById(song.getAlbumId())
                    .ifPresentOrElse(album -> dto.setAlbumName(album.getTitle()),
                            () -> dto.setAlbumName("Single"));
        } else {
            dto.setAlbumName("Single");
        }

        dto.setArtistId(song.getArtistId());
        dto.setAlbumId(song.getAlbumId());

        return dto;
    }

    private String cleanTitle(String title) {
        return title
                .replaceAll("(?i)\\b(official|music video|audio|lyrics|remix|live|mv|lyric video|visualizer)\\b.*$", "")
                .replaceAll("(?i)\\((official|audio|mv|lyrics|remix|live).*\\)", "")
                .replaceAll("[\\[\\](){}]", "")
                .trim();
    }
}