package com.example.beatboxcompany.Dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponseDto {
    private List<SongDto> trendingSongs;
    private List<ArtistDto> featuredArtists;
    private List<PlaylistDto> featuredPlaylists;
    private List<SongDto> recommendedSongs;
}
