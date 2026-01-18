package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Entity.Artist;
import com.example.beatboxcompany.Dto.AlbumDto;
import com.example.beatboxcompany.Dto.ArtistDto;
import com.example.beatboxcompany.Dto.SongDto;

import java.util.List;

public interface ArtistService {

    // 1. Tạo hồ sơ Artist mới (Sau khi User đăng ký role ARTIST)
    // Trong interface ArtistService.java
    ArtistDto createArtistByAdmin(String userId, String artistName);

    // 2. Lấy hồ sơ công khai
    ArtistDto getPublicArtistProfile(String artistId);

    // 3. Lấy hồ sơ của User đang đăng nhập để chỉnh sửa
    ArtistDto getCurrentArtistProfile(String userId);

    // 4. Cập nhật hồ sơ
    ArtistDto updateArtistProfile(String userId, ArtistDto updateDto);

    // 5. Tìm kiếm
    List<ArtistDto> searchArtists(String query);

    List<ArtistDto> getAllArtists();

    ArtistDto adminUpdateName(String artistId, String newName);

    List<ArtistDto> getArtistsByGenre(String genreSlug);

    ArtistDto syncArtistFromSpotify(String artistId, String spotifyArtistId);

    List<SongDto> getSongsByArtistId(String artistId);

    List<AlbumDto> getAlbumsByArtistId(String artistId);
}