package com.example.beatboxcompany.search.runner;

import com.example.beatboxcompany.Entity.*;
import com.example.beatboxcompany.Repository.*;
import com.example.beatboxcompany.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
    
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexerRunner implements CommandLineRunner {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    private final SearchIndexService searchIndexService;

    @Override
    public void run(String... args) {

        log.info("==============================");
        log.info("Building Search Index...");
        log.info("==============================");

        songRepository.findAll().forEach(searchIndexService::indexSong);

        artistRepository.findAll().forEach(searchIndexService::indexArtist);

        albumRepository.findAll().forEach(searchIndexService::indexAlbum);

        playlistRepository.findAll().forEach(searchIndexService::indexPlaylist);

        userRepository.findAll().forEach(searchIndexService::indexUser);

        log.info("==============================");
        log.info("Search Index Completed.");
        log.info("==============================");
    }
}