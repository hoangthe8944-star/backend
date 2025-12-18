package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Entity.Song;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SongService {

        // --- ADMIN / IMPORT ---
        Song createSong(String title, String artistName, String albumName,
                        String coverUrl, Integer durationSec, String spotifyId,
                        MultipartFile audioFile);

        void deleteSong(String id);

        List<SongDto> getSongsByStatus(String status);

        SongDto updateStatus(String songId, String newStatus);

        // --- ARTIST ---
        // Lấy bài hát do một user (nghệ sĩ) cụ thể tải lên
        List<SongDto> getSongsByUploader(String userId);

        // --- PUBLIC / USER ---
        List<SongDto> getAllSongs();

        List<SongDto> searchSongs(String keyword);

        SongDto getPublishedSongById(String id);

        void incrementViewCount(String id);

        SongDto createPendingSong(String title, String albumId, String coverUrl,
                        Integer duration, MultipartFile audioFile, String userEmail);

        void bulkImportFromZip(MultipartFile zipFile);

        List<SongDto> getTrendingPublishedSongs(int limit);

        void uploadSongFile(String songId, MultipartFile file) throws IOException;
        
        List<SongDto> searchPublicSongs(String query);

        void recordSongPlayback(String songId);

        List<SongDto> getRecentlyPlayedSongs(int limit);
}