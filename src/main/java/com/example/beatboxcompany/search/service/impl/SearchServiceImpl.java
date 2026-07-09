package com.example.beatboxcompany.search.service.impl;

import com.example.beatboxcompany.Entity.Song;
import com.example.beatboxcompany.Repository.SongRepository;
import com.example.beatboxcompany.search.dto.SearchResponse;
import com.example.beatboxcompany.search.dto.SearchSuggestResponse;
import com.example.beatboxcompany.search.entity.SearchIndex;
import com.example.beatboxcompany.search.repository.SearchRepository;
import com.example.beatboxcompany.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int TOP_RESULT_LIMIT = 5;
    private static final int SUGGESTION_LIMIT = 10;

    private final SearchRepository searchRepository;

    // Repository lấy dữ liệu gốc từ collection songs
    private final SongRepository songRepository;

    @Override
    public SearchResponse search(String keyword) {

        keyword = normalize(keyword);

        if (keyword.length() < MIN_KEYWORD_LENGTH) {
            return emptySearchResponse();
        }

        List<SearchIndex> results = searchRepository.findByKeyword(keyword)
                .stream()
                .map(this::hydrateSearchItem)
                .toList();

        List<SearchIndex> topResults = new ArrayList<>();
        List<SearchIndex> songs = new ArrayList<>();
        List<SearchIndex> artists = new ArrayList<>();
        List<SearchIndex> albums = new ArrayList<>();
        List<SearchIndex> playlists = new ArrayList<>();
        List<SearchIndex> users = new ArrayList<>();

        for (SearchIndex item : results) {

            if (topResults.size() < TOP_RESULT_LIMIT) {
                topResults.add(item);
            }

            if (item.getType() == null) {
                continue;
            }

            switch (item.getType()) {

                case "SONG" -> songs.add(item);

                case "ARTIST" -> artists.add(item);

                case "ALBUM" -> albums.add(item);

                case "PLAYLIST" -> playlists.add(item);

                case "USER" -> users.add(item);

                default -> {
                    // Bỏ qua type không hợp lệ
                }
            }
        }

        return SearchResponse.builder()
                .topResults(topResults)
                .songs(songs)
                .artists(artists)
                .albums(albums)
                .playlists(playlists)
                .users(users)
                .build();
    }

    @Override
    public SearchSuggestResponse suggest(String keyword) {

        keyword = normalize(keyword);

        if (keyword.length() < MIN_KEYWORD_LENGTH) {
            return SearchSuggestResponse.builder()
                    .keyword(keyword)
                    .suggestions(List.of())
                    .build();
        }

        List<String> suggestions = searchRepository.findByKeyword(keyword)
                .stream()
                .map(SearchIndex::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .distinct()
                .limit(SUGGESTION_LIMIT)
                .toList();

        return SearchSuggestResponse.builder()
                .keyword(keyword)
                .suggestions(suggestions)
                .build();
    }

    private SearchIndex hydrateSearchItem(SearchIndex item) {

        if (item == null || item.getType() == null) {
            return item;
        }

        if (!"SONG".equals(item.getType())) {
            return item;
        }

        String songId = item.getReferenceId() != null
                ? item.getReferenceId()
                : item.getId();

        if (songId == null || songId.isBlank()) {
            return item;
        }

        songRepository.findById(songId).ifPresent(song -> fillSongData(item, song));

        return item;
    }

    private void fillSongData(SearchIndex item, Song song) {

        item.setReferenceId(song.getId());
        item.setTitle(song.getTitle());
        item.setSubtitle(song.getArtistName());
        item.setArtistName(song.getArtistName());
        item.setDescription(song.getArtistName());
        item.setSearchText(
                (safe(song.getTitle()) + " " + safe(song.getArtistName())).trim()
        );

        item.setArtistId(song.getArtistId());
        item.setAlbumId(song.getAlbumId());
        item.setCategoryId(song.getCategoryId());

        // Phần quan trọng để frontend phát nhạc
        item.setStreamUrl(song.getStreamUrl());
        item.setAudioUrl(song.getAudioUrl());
        item.setFilePath(song.getFilePath());
        item.setStreamPublicId(song.getStreamPublicId());

        item.setImageUrl(
                song.getCoverImageUrl() != null
                        ? song.getCoverImageUrl()
                        : song.getCoverUrl()
        );

        item.setCoverUrl(song.getCoverUrl());
        item.setCoverImageUrl(song.getCoverImageUrl());
        item.setCoverPublicId(song.getCoverPublicId());

        item.setDuration(song.getDuration());
        item.setDurationMs(song.getDurationMs());
        item.setGenres(song.getGenres());
        item.setViewCount(song.getViewCount());
        item.setPopularity(song.getViewCount());
        item.setExplicit(song.getExplicit());
        item.setStatus(song.getStatus());
        item.setSpotifyId(song.getSpotifyId());
        item.setCreatedAt(song.getCreatedAt());
        item.setLastPlayedAt(song.getLastPlayedAt());
    }

    private SearchResponse emptySearchResponse() {
        return SearchResponse.builder()
                .topResults(List.of())
                .songs(List.of())
                .artists(List.of())
                .albums(List.of())
                .playlists(List.of())
                .users(List.of())
                .build();
    }

    private String normalize(String keyword) {

        if (keyword == null) {
            return "";
        }

        return keyword.trim().toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}