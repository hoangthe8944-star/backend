package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Request.SongUploadRequest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SongMapper {
    @Value("${app.public-url}")
    private String publicUrl;

    // Chuyển từ Request (Artist upload) sang Entity
    public Song toEntity(SongUploadRequest request, String artistId) {
        Song song = new Song();
        song.setTitle(request.getTitle());
        song.setArtistId(artistId);
        song.setAlbumId(request.getAlbumId());

        song.setDuration(request.getDuration());
        // Nếu có trường durationMs trong Entity thì nên tính luôn
        if (request.getDuration() != null) {
            song.setDurationMs(request.getDuration() * 1000L);
        }

        song.setStreamUrl(request.getStreamUrl());
        song.setCoverUrl(request.getCoverUrl());

        // Xử lý null cho List
        song.setGenre(request.getGenre() != null ? request.getGenre() : Collections.emptyList());

        // Xử lý null cho Boolean
        song.setIsExplicit(request.getIsExplicit() != null && request.getIsExplicit());

        song.setViewCount(0L);
        song.setStatus("PENDING");

        return song;
    }

    // Chuyển từ Entity sang DTO
    public SongDto toDto(Song song) {
        SongDto dto = new SongDto();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setDuration(song.getDuration());

        // --- LOGIC TẠO STREAM URL ---
        // Nếu có filePath (file upload lên server), ưu tiên tạo link stream nội bộ
        if (song.getFilePath() != null && !song.getFilePath().isEmpty()) {
            dto.setStreamUrl(
                    publicUrl + "/api/public/songs/stream/" + song.getFilePath());
        } else {
            dto.setStreamUrl(song.getStreamUrl());
        }

        dto.setCoverUrl(song.getCoverUrl());

        // Map các trường tham chiếu (Bắt buộc SongDto phải có các trường này)
        dto.setArtistId(song.getArtistId());
        dto.setAlbumId(song.getAlbumId());
        dto.setGenre(song.getGenre());
        dto.setViewCount(song.getViewCount());
        dto.setIsExplicit(song.getIsExplicit());
        dto.setStatus(song.getStatus());

        // Lưu ý: Mapper này chưa map artistName và albumName vì không gọi Repository.
        // Việc đó thường làm ở Service. Nếu muốn làm ở đây, phải Inject Repository vào.

        return dto;
    }
}