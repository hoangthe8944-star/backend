package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Entity.Playlist;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Request.PlaylistRequest;

import java.util.ArrayList;

public class PlaylistMapper {

    // Chuyển PlaylistRequest + ownerId -> Playlist entity
    public static Playlist toEntity(PlaylistRequest request, String ownerId) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setType(request.getType());
        playlist.setPublicPlaylist(request.getIsPublic() != null ? request.getIsPublic() : false);
        playlist.setOwnerId(ownerId);
        playlist.setTracks(request.getTracks() != null ? request.getTracks() : new ArrayList<>());
        playlist.setCoverImage(request.getCoverImage());
        return playlist;
    }

    // Chuyển Playlist entity -> PlaylistDto
    public static PlaylistDto toDto(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        System.err.println("DEBUG MAPPER: Đang gán ownerId từ Entity (" + playlist.getOwnerId() + ") vào DTO");
        dto.setOwnerId(playlist.getOwnerId());
        dto.setPublicPlaylist(playlist.isPublicPlaylist());
        dto.setType(playlist.getType());
        dto.setTracks(playlist.getTracks() != null ? playlist.getTracks() : new ArrayList<>());
        dto.setCoverImage(playlist.getCoverImage());
        return dto;
    }
}
