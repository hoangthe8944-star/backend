package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.HomeResponseDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.PlaylistDto;
import com.example.beatboxcompany.Service.HomeService;
import com.example.beatboxcompany.Service.SongService;
import com.example.beatboxcompany.Service.ArtistService;
import com.example.beatboxcompany.Service.PlaylistService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {

    private final SongService songService;
    private final ArtistService artistService;
    private final PlaylistService playlistService;

    public HomeServiceImpl(SongService songService, ArtistService artistService, PlaylistService playlistService) {
        this.songService = songService;
        this.artistService = artistService;
        this.playlistService = playlistService;
    }

    @Override
    @Cacheable(value = "home")
    public HomeResponseDto getHomeData() {
        List<SongDto> trending = songService.getTrendingPublishedSongs(18);
        List<ArtistDto> artists = artistService.getAllArtists().stream().limit(6).collect(Collectors.toList());
        List<PlaylistDto> playlists = playlistService.getPublicPlaylists().stream().limit(6).collect(Collectors.toList());
        List<SongDto> allSongs = songService.getSongsByStatus("PUBLISHED");
        
        // Shuffle to get random recommended songs
        Collections.shuffle(allSongs);
        List<SongDto> recommended = allSongs.stream().limit(15).collect(Collectors.toList());

        return new HomeResponseDto(trending, artists, playlists, recommended);
    }
}
