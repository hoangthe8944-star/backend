package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.EpisodeDto;
import com.example.beatboxcompany.Dto.PodcastDto;
import com.example.beatboxcompany.Request.EpisodeRequest;
import com.example.beatboxcompany.Request.PodcastRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PodcastService {
    // Podcast CRUD
    PodcastDto createPodcast(PodcastRequest request, MultipartFile coverImageFile);
    PodcastDto getPodcastById(String id);
    List<PodcastDto> getAllPodcasts();
    PodcastDto updatePodcast(String id, PodcastRequest request, MultipartFile coverImageFile);
    void deletePodcast(String id);

    // Episode CRUD
    EpisodeDto createEpisode(String podcastId, EpisodeRequest request, MultipartFile audioFile);
    EpisodeDto getEpisodeById(String id);
    List<EpisodeDto> getEpisodesByPodcastId(String podcastId);
    EpisodeDto updateEpisode(String episodeId, EpisodeRequest request, MultipartFile audioFile);
    void deleteEpisode(String episodeId);

    // Additional Features
    void incrementEpisodePlayCount(String episodeId);
}
