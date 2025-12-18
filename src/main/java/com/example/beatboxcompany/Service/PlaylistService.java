package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;

import java.util.List;

public interface PlaylistService {
    
    // 1. Tạo Playlist mới
    PlaylistDto createPlaylist(PlaylistRequest request, String ownerId);
    
    // 2. Lấy tất cả Playlist của User
    List<PlaylistDto> getUserPlaylists(String ownerId);

    // 3. Lấy chi tiết Playlist công khai
    PlaylistDto getPublicPlaylistDetails(String playlistId);
    
    // 4. Thêm bài hát vào Playlist
    PlaylistDto addTrackToPlaylist(String playlistId, String trackId, String currentUserId);
    
    // 5. Xóa bài hát khỏi Playlist
    PlaylistDto removeTrackFromPlaylist(String playlistId, String trackId, String currentUserId);

    // 6. Xóa Playlist
    void deletePlaylist(String playlistId, String currentUserId);
}