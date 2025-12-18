package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Entity.Playlist;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;

public class PlaylistMapper {

    // Chuyển từ Request DTO sang Entity (khi tạo mới)
    public static Playlist toEntity(PlaylistRequest request, String ownerId) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setOwnerId(ownerId);
        // Thiết lập mặc định nếu Client không gửi isPublic
        playlist.setPublic(request.getIsPublic() != null ? request.getIsPublic() : false); 
        if (request.getTracks() != null) {
            playlist.setTracks(request.getTracks());
        }
        return playlist;
    }

    // Chuyển từ Entity sang Response DTO
    public static PlaylistDto toDto(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setOwnerId(playlist.getOwnerId());
        dto.setPublic(playlist.isPublic());
        dto.setTracks(playlist.getTracks());
        return dto;
    }
}