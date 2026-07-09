package com.example.beatboxcompany.search.controller;

import com.example.beatboxcompany.Entity.*;
import com.example.beatboxcompany.Repository.*;
import com.example.beatboxcompany.search.repository.SearchRepository;
import com.example.beatboxcompany.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
public class SearchAdminController {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    private final SearchRepository searchRepository;
    private final SearchIndexService searchIndexService;

    @PostMapping("/rebuild")
    public ResponseEntity<String> rebuild() {

        searchRepository.deleteAll();

        songRepository.findAll().forEach(searchIndexService::indexSong);

        artistRepository.findAll().forEach(searchIndexService::indexArtist);

        albumRepository.findAll().forEach(searchIndexService::indexAlbum);

        playlistRepository.findAll().forEach(searchIndexService::indexPlaylist);

        userRepository.findAll().forEach(searchIndexService::indexUser);

        return ResponseEntity.ok("Search index rebuilt successfully.");
    }
}