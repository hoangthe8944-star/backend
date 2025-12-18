package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.TrackDto;
import com.example.beatboxcompany.Entity.Album;
import com.example.beatboxcompany.Repository.ArtistRepository;
import com.example.beatboxcompany.Repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlbumMapper {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private SongRepository songRepository;

    // --- 1. Mapper chi tiết (Dùng khi xem chi tiết Album) ---
    public AlbumDto toFullDto(Album album) {
        AlbumDto dto = toSimpleDto(album); // Tái sử dụng logic map cơ bản

        // Map danh sách bài hát (Tracks)
        if (album.getTrackIds() != null && !album.getTrackIds().isEmpty()) {
            List<TrackDto> tracks = album.getTrackIds().stream()
                .map(songId -> songRepository.findById(songId).orElse(null))
                .filter(song -> song != null)
                .map(song -> {
                    TrackDto trackDto = new TrackDto();
                    trackDto.setSongId(song.getId());
                    trackDto.setTitle(song.getTitle());
                    trackDto.setDurationMs(song.getDurationMs());
                    // Format thời lượng track: mm:ss
                    long s = (song.getDurationMs() / 1000) % 60;
                    long m = (song.getDurationMs() / 1000) / 60;
                    trackDto.setDurationText(String.format("%02d:%02d", m, s));
                    
                    trackDto.setIsExplicit(song.getIsExplicit() != null && song.getIsExplicit());
                    trackDto.setStreamUrl("http://localhost:8080/api/public/songs/stream/" + song.getFilePath());
                    return trackDto;
                })
                .collect(Collectors.toList());

            dto.setTracks(tracks);
        } else {
            dto.setTracks(new ArrayList<>());
        }

        return dto;
    }

    // --- 2. Mapper đơn giản (Dùng cho danh sách Admin/Home) ---
    public AlbumDto toSimpleDto(Album album) {
        AlbumDto dto = new AlbumDto();
        dto.setId(album.getId());
        dto.setTitle(album.getTitle());
        dto.setReleaseDate(album.getReleaseDate());
        dto.setCoverUrl(album.getCoverUrl());
        dto.setStatus(album.getStatus());
        
        // Map các trường thống kê (Khớp với AlbumDto đã sửa)
        dto.setSongCount(album.getTotalTracks()); 
        
        // Format thời lượng tổng (mm:ss)
        long totalMs = album.getTotalDurationMs() != null ? album.getTotalDurationMs() : 0;
        long minutes = (totalMs / 1000) / 60;
        long seconds = (totalMs / 1000) % 60;
        dto.setTotalDuration(String.format("%02d:%02d", minutes, seconds));
        
        dto.setTotalStreams(0L); // Fake data hoặc tính tổng sau

        // Lấy tên Artist
        if (album.getArtistId() != null) {
            artistRepository.findById(album.getArtistId())
                    .ifPresentOrElse(
                        artist -> dto.setArtistName(artist.getName()),
                        () -> dto.setArtistName("Unknown Artist")
                    );
        }

        return dto;
    }
}