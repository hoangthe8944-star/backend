package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.TrackDto; // Đảm bảo đã import
import com.example.beatboxcompany.Entity.Album;
import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Repository.AlbumRepository;
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Repository.SongRepository;
import com.example.beatboxcompany.Request.AlbumRequest;
import com.example.beatboxcompany.Request.TrackRequest;
import com.example.beatboxcompany.Service.AlbumService;
import com.example.beatboxcompany.Service.SpotifyService; // Import SpotifyService
import lombok.RequiredArgsConstructor; // Import Lombok
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // <--- DÒNG NÀY QUAN TRỌNG: Nó tự tạo Constructor cho các biến final
public class AlbumServiceImpl implements AlbumService {

    // Khai báo các dependency cần dùng (bắt buộc phải có final)
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final SpotifyService spotifyService; // <--- Chỉ cần khai báo dòng này, Lombok tự Inject

    // --- KHÔNG CẦN VIẾT CONSTRUCTOR THỦ CÔNG NỮA ---

    // --- 1. IMPORT TỪ SPOTIFY (Logic mới) ---
    @Override
    @Transactional
    public void importAlbumFromSpotify(String spotifyAlbumId) {
        // 1. Lấy dữ liệu từ Spotify
        Map<String, Object> data = spotifyService.getAlbumDetails(spotifyAlbumId);
        
        String albumTitle = (String) data.get("name");
        String releaseDateStr = (String) data.get("release_date");
        
        List<Map<String, Object>> images = (List<Map<String, Object>>) data.get("images");
        String coverUrl = images != null && !images.isEmpty() ? (String) images.get(0).get("url") : "";

        List<Map<String, Object>> artists = (List<Map<String, Object>>) data.get("artists");
        String artistName = (String) artists.get(0).get("name");

        // 2. Tìm hoặc Tạo Artist
        Artist artist = artistRepository.findByName(artistName)
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(artistName);
                    newArtist.setUserId("spotify_import_" + System.currentTimeMillis());
                    newArtist.setCreatedAt(LocalDateTime.now());
                    newArtist.setVerified(true);
                    return artistRepository.save(newArtist);
                });

        // 3. Tạo Album
        Album album = new Album();
        album.setTitle(albumTitle);
        album.setArtistId(artist.getId());
        album.setCoverUrl(coverUrl);
        album.setStatus("PUBLISHED"); 
        
        try {
            // Xử lý ngày tháng (Spotify trả về chuỗi, ta cần LocalDate)
            // Nếu chuỗi chỉ có năm "2023", thêm "-01-01" để parse
            if (releaseDateStr.length() == 4) releaseDateStr += "-01-01";
            album.setReleaseDate(java.time.LocalDate.parse(releaseDateStr));
        } catch (Exception e) {
            album.setReleaseDate(java.time.LocalDate.now());
        }
        
        album.setCreatedAt(LocalDateTime.now());
        album.setUpdatedAt(LocalDateTime.now());
        album.setTrackIds(new ArrayList<>());

        // 4. Xử lý Tracks
        Map<String, Object> tracksObj = (Map<String, Object>) data.get("tracks");
        List<Map<String, Object>> items = (List<Map<String, Object>>) tracksObj.get("items");
        
        long totalDuration = 0;

        for (Map<String, Object> item : items) {
            Song song = new Song();
            song.setTitle((String) item.get("name"));
            song.setArtistId(artist.getId());
            song.setSpotifyId((String) item.get("id"));
            
            int durationMs = (Integer) item.get("duration_ms");
            song.setDurationMs((long) durationMs);
            song.setDuration(durationMs / 1000);
            
            song.setCoverUrl(coverUrl);
            song.setStreamUrl((String) item.get("preview_url")); 
            song.setFilePath(null); // Chưa có file thật
            song.setStatus("PENDING"); // Để pending chờ upload file
            song.setCreatedAt(LocalDateTime.now());

            song = songRepository.save(song);
            
            album.getTrackIds().add(song.getId());
            totalDuration += durationMs;
        }

        album.setTotalTracks(items.size());
        album.setTotalDurationMs(totalDuration);

        albumRepository.save(album);
    }

    // --- 2. CÁC HÀM CŨ (Giữ nguyên logic) ---

    @Override
    public List<AlbumDto> getAllAlbums() {
        return albumRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlbumDto createPendingAlbum(AlbumRequest request, String currentUserId) {
        // ... (Giữ nguyên code cũ của bạn ở đây) ...
        return null; // (Demo placeholder, bạn paste lại code cũ vào)
    }

    @Override
    public List<AlbumDto> getAlbumsByStatus(String status) {
        return albumRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlbumDto updateAlbumStatus(String albumId, String status, String reason) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        album.setStatus(status);
        album.setRejectedReason(reason);
        return convertToDto(albumRepository.save(album));
    }

    @Override
    public AlbumDto approveAlbum(String albumId) {
        return updateAlbumStatus(albumId, "PUBLISHED", null);
    }

    @Override
    public AlbumDto rejectAlbum(String albumId, String reason) {
        return updateAlbumStatus(albumId, "REJECTED", reason);
    }

    @Override
    public AlbumDto getPublishedAlbumById(String albumId) {
        return convertToDto(albumRepository.findById(albumId).orElse(null));
    }

    @Override
    public List<AlbumDto> getAlbumsByUploader(String currentUserId) {
        return List.of(); // Placeholder
    }

    // --- MAPPER ---
    private AlbumDto convertToDto(Album album) {
        if (album == null) return null;
        
        AlbumDto dto = new AlbumDto();
        dto.setId(album.getId());
        dto.setTitle(album.getTitle());
        dto.setCoverUrl(album.getCoverUrl());
        dto.setReleaseDate(album.getReleaseDate());
        dto.setStatus(album.getStatus());

        if (album.getArtistId() != null) {
            artistRepository.findById(album.getArtistId())
                    .ifPresentOrElse(
                        artist -> dto.setArtistName(artist.getName()),
                        () -> dto.setArtistName("Unknown")
                    );
        }

        dto.setSongCount(album.getTotalTracks());
        
        long ms = album.getTotalDurationMs() != null ? album.getTotalDurationMs() : 0;
        long minutes = (ms / 1000) / 60;
        long seconds = (ms / 1000) % 60;
        dto.setTotalDuration(String.format("%02d:%02d", minutes, seconds));
        
        dto.setTotalStreams(0L);

        // Map Tracks nếu cần hiển thị chi tiết (Dùng TrackDto đã tạo ở bài trước)
        if (album.getTrackIds() != null) {
            List<TrackDto> tracks = album.getTrackIds().stream()
                .map(songId -> songRepository.findById(songId).orElse(null))
                .filter(s -> s != null)
                .map(s -> {
                    TrackDto t = new TrackDto();
                    t.setSongId(s.getId());
                    t.setTitle(s.getTitle());
                    t.setDurationMs(s.getDurationMs());
                    t.setDurationText(String.format("%02d:%02d", (s.getDurationMs()/1000)/60, (s.getDurationMs()/1000)%60));
                    return t;
                })
                .collect(Collectors.toList());
            dto.setTracks(tracks);
        }

        return dto;
    }
}