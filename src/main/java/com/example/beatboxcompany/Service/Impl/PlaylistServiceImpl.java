package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.Playlist;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;
import com.example.beatboxcompany.Mapper.PlaylistMapper;
import com.example.beatboxcompany.Repository.PlaylistRepository;
import com.example.beatboxcompany.Service.PlaylistService;
import com.example.beatboxcompany.Service.SongService; // Cần dùng SongService để kiểm tra bài hát

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongService songService; // Để kiểm tra bài hát có tồn tại và đã PUBLISHED

    public PlaylistServiceImpl(PlaylistRepository playlistRepository, SongService songService) {
        this.playlistRepository = playlistRepository;
        this.songService = songService;
    }

    // Helper: Kiểm tra quyền sở hữu
    private void checkOwner(Playlist playlist, String currentUserId) {
        if (!playlist.getOwnerId().equals(currentUserId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa Playlist này.");
        }
    }

    @Override
    public PlaylistDto createPlaylist(PlaylistRequest request, String ownerId) {
        Playlist playlist = PlaylistMapper.toEntity(request, ownerId);
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistMapper.toDto(savedPlaylist);
    }

    @Override
    public List<PlaylistDto> getUserPlaylists(String ownerId) {
        return playlistRepository.findByOwnerId(ownerId).stream()
            .map(PlaylistMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public PlaylistDto getPublicPlaylistDetails(String playlistId) {
        // Chỉ lấy Playlist đã được đánh dấu là public
        Playlist playlist = playlistRepository.findByIdAndIsPublic(playlistId, true)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Playlist công khai này!"));
        return PlaylistMapper.toDto(playlist);
    }

    @Override
    public PlaylistDto addTrackToPlaylist(String playlistId, String trackId, String currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist không tồn tại."));
            
        // 1. Kiểm tra quyền
        checkOwner(playlist, currentUserId);

        // 2. Kiểm tra bài hát có tồn tại và đã PUBLISHED (Nghiệp vụ quan trọng!)
        // songService.getPublishedSongById(trackId); // Gọi Service để đảm bảo tính toàn vẹn
        
        // 3. Thêm bài hát (Nếu chưa có)
        if (!playlist.getTracks().contains(trackId)) {
            playlist.getTracks().add(trackId);
            playlistRepository.save(playlist);
        }
        
        return PlaylistMapper.toDto(playlist);
    }
    
    @Override
    public PlaylistDto removeTrackFromPlaylist(String playlistId, String trackId, String currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist không tồn tại."));
            
        // 1. Kiểm tra quyền
        checkOwner(playlist, currentUserId);

        // 2. Xóa bài hát
        playlist.getTracks().remove(trackId);
        playlistRepository.save(playlist);
        
        return PlaylistMapper.toDto(playlist);
    }
    
    @Override
    public void deletePlaylist(String playlistId, String currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist không tồn tại."));

        // 1. Kiểm tra quyền
        checkOwner(playlist, currentUserId);
        
        // 2. Xóa
        playlistRepository.delete(playlist);
    }
}