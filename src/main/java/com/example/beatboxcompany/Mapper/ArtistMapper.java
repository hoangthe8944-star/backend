package com.example.beatboxcompany.Mapper;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Dto.ArtistDto;

public class ArtistMapper {

    // Chuyển từ Entity sang DTO để trả về Client
    public static ArtistDto toDto(Artist artist) {
        ArtistDto dto = new ArtistDto();
        dto.setId(artist.getId());
        dto.setName(artist.getName());
        dto.setBio(artist.getBio());
        dto.setAvatarUrl(artist.getAvatarUrl());
        dto.setFollowerCount(artist.getFollowerCount());
        return dto;
    }
    
    // Chuyển từ DTO (hoặc Request) sang Entity để cập nhật
    public static Artist updateEntity(Artist existingArtist, ArtistDto updateDto) {
        existingArtist.setName(updateDto.getName());
        existingArtist.setBio(updateDto.getBio());
        existingArtist.setAvatarUrl(updateDto.getAvatarUrl());
        return existingArtist;
    }
}