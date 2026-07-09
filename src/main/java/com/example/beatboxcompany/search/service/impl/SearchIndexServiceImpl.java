package com.example.beatboxcompany.search.service.impl;

import com.example.beatboxcompany.Entity.*;
import com.example.beatboxcompany.search.entity.SearchIndex;
import com.example.beatboxcompany.search.repository.SearchRepository;
import com.example.beatboxcompany.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchIndexServiceImpl implements SearchIndexService {

    private final SearchRepository searchRepository;

    @Override
    public void indexSong(Song song) {

        String imageUrl = song.getCoverImageUrl() != null
                ? song.getCoverImageUrl()
                : song.getCoverUrl();

        String searchText = safe(song.getTitle()) + " " + safe(song.getArtistName());

        SearchIndex index = SearchIndex.builder()
                .id(song.getId())
                .type("SONG")
                .referenceId(song.getId())

                // Thông tin tìm kiếm
                .title(song.getTitle())
                .subtitle(song.getArtistName())
                .artistName(song.getArtistName())
                .description(song.getArtistName())
                .searchText(searchText.trim())

                // Thông tin liên kết
                .artistId(song.getArtistId())
                .albumId(song.getAlbumId())
                .categoryId(song.getCategoryId())

                // Thông tin phát nhạc - PHẦN QUAN TRỌNG
                .streamUrl(song.getStreamUrl())
                .audioUrl(song.getAudioUrl())
                .filePath(song.getFilePath())
                .streamPublicId(song.getStreamPublicId())

                // Thông tin ảnh
                .imageUrl(imageUrl)
                .coverUrl(song.getCoverUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .coverPublicId(song.getCoverPublicId())

                // Metadata bài hát
                .duration(song.getDuration())
                .durationMs(song.getDurationMs())
                .genres(song.getGenres())
                .viewCount(song.getViewCount())
                .popularity(song.getViewCount())
                .explicit(song.getExplicit())
                .status(song.getStatus())
                .spotifyId(song.getSpotifyId())
                .createdAt(song.getCreatedAt())
                .lastPlayedAt(song.getLastPlayedAt())

                .build();

        searchRepository.save(index);
    }

    @Override
    public void indexArtist(Artist artist) {

        String description = "";

        if (artist.getGenres() != null && !artist.getGenres().isEmpty()) {
            description = String.join(" ", artist.getGenres());
        }

        String searchText = safe(artist.getName()) + " " + safe(description);

        SearchIndex index = SearchIndex.builder()
                .id(artist.getId())
                .type("ARTIST")
                .referenceId(artist.getId())

                .title(artist.getName())
                .subtitle("Artist")
                .description(description)
                .searchText(searchText.trim())

                .imageUrl(
                        artist.getAvatarUrl() != null
                                ? artist.getAvatarUrl()
                                : artist.getImageUrl()
                )

                .build();

        searchRepository.save(index);
    }

    @Override
    public void indexAlbum(Album album) {

        String searchText = safe(album.getName()) + " " + safe(album.getAlbumType());

        SearchIndex index = SearchIndex.builder()
                .id(album.getId())
                .type("ALBUM")
                .referenceId(album.getId())

                .title(album.getName())
                .subtitle("Album")
                .description(album.getAlbumType())
                .searchText(searchText.trim())

                .imageUrl(album.getCoverImageUrl())
                .coverImageUrl(album.getCoverImageUrl())

                .build();

        searchRepository.save(index);
    }

    @Override
    public void indexPlaylist(Playlist playlist) {

        String searchText = safe(playlist.getName()) + " " + safe(playlist.getDescription());

        SearchIndex index = SearchIndex.builder()
                .id(playlist.getId())
                .type("PLAYLIST")
                .referenceId(playlist.getId())

                .title(playlist.getName())
                .subtitle("Playlist")
                .description(playlist.getDescription())
                .searchText(searchText.trim())

                .imageUrl(playlist.getCoverImage())
                .coverImageUrl(playlist.getCoverImage())

                .build();

        searchRepository.save(index);
    }

    @Override
    public void indexUser(User user) {

        String searchText = safe(user.getUsername()) + " " + safe(user.getEmail());

        SearchIndex index = SearchIndex.builder()
                .id(user.getId())
                .type("USER")
                .referenceId(user.getId())

                .title(user.getUsername())
                .subtitle("User")
                .description(user.getEmail())
                .searchText(searchText.trim())

                .imageUrl(user.getAvatarUrl())
                .coverImageUrl(user.getAvatarUrl())

                .build();

        searchRepository.save(index);
    }

    @Override
    public void rebuildIndex() {
        // TODO:
        // Muốn rebuild đầy đủ thì cần inject thêm:
        // SongRepository, ArtistRepository, AlbumRepository, PlaylistRepository, UserRepository
        // Sau đó delete toàn bộ search_index và index lại từng collection.
    }

    @Override
    public void remove(String referenceId) {
        searchRepository.deleteByReferenceId(referenceId);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}