package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.Playlist;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Dto.SongDto; // Đảm bảo bạn đã import SongDto
import com.example.beatboxcompany.Request.PlaylistRequest;
import com.example.beatboxcompany.Mapper.PlaylistMapper;
import com.example.beatboxcompany.Repository.PlaylistRepository;
import com.example.beatboxcompany.Service.PlaylistService;
import com.example.beatboxcompany.Service.SongService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongService songService;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository, SongService songService) {
        this.playlistRepository = playlistRepository;
        this.songService = songService;
    }

    /**
     * HÀM HỖ TRỢ: Nạp thông tin chi tiết bài hát vào PlaylistDto
     * Giúp Frontend có dữ liệu ảnh, tên bài hát ngay lập tức (Hết lỗi 404)
     */
    private PlaylistDto enrichPlaylistDto(Playlist playlist) {
        PlaylistDto dto = PlaylistMapper.toDto(playlist);
        
        if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {
            List<SongDto> details = playlist.getTracks().stream()
                .map(trackId -> {
                    try {
                        // Gọi songService để lấy chi tiết bài hát (bao gồm coverUrl, title...)
                        return songService.getPublishedSongById(trackId);
                    } catch (Exception e) {
                        return null; // Bỏ qua nếu bài hát không tồn tại hoặc lỗi
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            dto.setSongDetails(details);
            dto.setSongCount(details.size());
        } else {
            dto.setSongDetails(new ArrayList<>());
            dto.setSongCount(0);
        }
        
        return dto;
    }

    // ----- Kiểm tra quyền chỉnh sửa -----
    private void checkEditPermission(Playlist playlist, String currentUserId, boolean isAdmin) {
        if (isAdmin) return; // Admin luôn có quyền

        switch (playlist.getType()) {
            case "user":
                if (playlist.getOwnerId() == null || !playlist.getOwnerId().equals(currentUserId)) {
                    throw new SecurityException("Bạn không có quyền chỉnh sửa playlist này");
                }
                break;
            case "editorial":
                if (!isAdmin) {
                    throw new SecurityException("Chỉ admin mới được chỉnh sửa playlist editorial");
                }
                break;
            case "system":
                throw new SecurityException("Playlist hệ thống không thể chỉnh sửa");
            default:
                throw new IllegalStateException("Playlist type không hợp lệ");
        }
    }

    // ----- Tạo playlist -----
    @Override
    public PlaylistDto createPlaylist(PlaylistRequest request, String ownerId, boolean isAdmin) {
        if ("system".equals(request.getType()) && !isAdmin) {
            throw new SecurityException("Chỉ admin mới được tạo playlist hệ thống");
        }

        if (request.getTracks() == null) {
            request.setTracks(new ArrayList<>());
        }

        Playlist playlist = PlaylistMapper.toEntity(request, ownerId);
        // Đảm bảo ownerId được gán đúng
        playlist.setOwnerId(ownerId); 

        Playlist saved = playlistRepository.save(playlist);
        return enrichPlaylistDto(saved);
    }

    // ----- Cập nhật playlist -----
    @Override
    public PlaylistDto updatePlaylist(String playlistId, PlaylistRequest request, String currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));

        checkEditPermission(playlist, currentUserId, isAdmin);

        if (request.getName() != null) playlist.setName(request.getName());
        if (request.getDescription() != null) playlist.setDescription(request.getDescription());
        if (request.getType() != null) playlist.setType(request.getType());
        if (request.getIsPublic() != null) playlist.setPublicPlaylist(request.getIsPublic());
        if (request.getTracks() != null) playlist.setTracks(request.getTracks());
        if (request.getCoverImage() != null) playlist.setCoverImage(request.getCoverImage());

        Playlist saved = playlistRepository.save(playlist);
        return enrichPlaylistDto(saved);
    }

    // ----- Lấy playlist của user -----
    @Override
    public List<PlaylistDto> getUserPlaylists(String ownerId) {
        return playlistRepository.findByOwnerId(ownerId).stream()
                .map(this::enrichPlaylistDto) // Nạp chi tiết cho từng playlist trong danh sách
                .collect(Collectors.toList());
    }

    // ----- Lấy chi tiết playlist public -----
    @Override
    public PlaylistDto getPublicPlaylistDetails(String playlistId) {
        Playlist playlist = playlistRepository.findByIdAndPublicPlaylist(playlistId, true)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Playlist công khai này!"));
        return enrichPlaylistDto(playlist);
    }

    // ----- Lấy danh sách tất cả playlist public -----
    @Override
    public List<PlaylistDto> getPublicPlaylists() {
        return playlistRepository.findByPublicPlaylist(true).stream()
                .map(this::enrichPlaylistDto)
                .collect(Collectors.toList());
    }

    // ----- Thêm track vào playlist -----
    @Override
    public PlaylistDto addTrackToPlaylist(String playlistId, String trackId, String currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại"));

        checkEditPermission(playlist, currentUserId, isAdmin);
        songService.getPublishedSongById(trackId);

        List<String> tracks = playlist.getTracks();
        if (!tracks.contains(trackId)) {
            tracks.add(trackId);
            playlist.setTracks(tracks);
            playlistRepository.save(playlist);
        }

        return enrichPlaylistDto(playlist);
    }

    // ----- Xóa track khỏi playlist -----
    @Override
    public PlaylistDto removeTrackFromPlaylist(String playlistId, String trackId, String currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại."));

        checkEditPermission(playlist, currentUserId, false);

        List<String> tracks = playlist.getTracks();
        if (tracks.contains(trackId)) {
            tracks.remove(trackId);
            playlist.setTracks(tracks);
            playlistRepository.save(playlist);
        }

        return enrichPlaylistDto(playlist);
    }

    // ----- Xóa playlist -----
    @Override
    public void deletePlaylist(String playlistId, String currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist không tồn tại."));

        checkEditPermission(playlist, currentUserId, isAdmin);
        playlistRepository.delete(playlist);
    }
}