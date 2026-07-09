package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.EpisodeDto;
import com.example.beatboxcompany.Dto.PodcastDto;
import com.example.beatboxcompany.Dto.UploadResultDto;
import com.example.beatboxcompany.Entity.Episode;
import com.example.beatboxcompany.Entity.Podcast;
import com.example.beatboxcompany.Repository.EpisodeRepository;
import com.example.beatboxcompany.Repository.PodcastRepository;
import com.example.beatboxcompany.Request.EpisodeRequest;
import com.example.beatboxcompany.Request.PodcastRequest;
import com.example.beatboxcompany.Service.CloudinaryService;
import com.example.beatboxcompany.Service.PodcastService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PodcastServiceImpl implements PodcastService {

    private final PodcastRepository podcastRepository;
    private final EpisodeRepository episodeRepository;
    private final CloudinaryService cloudinaryService;

    public PodcastServiceImpl(PodcastRepository podcastRepository,
                              EpisodeRepository episodeRepository,
                              CloudinaryService cloudinaryService) {
        this.podcastRepository = podcastRepository;
        this.episodeRepository = episodeRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // --- Podcast CRUD ---

    @Override
    public PodcastDto createPodcast(PodcastRequest request, MultipartFile coverImageFile) {
        String coverUrl = null;
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            UploadResultDto uploadResult = cloudinaryService.uploadFile(coverImageFile, "podcast_covers");
            coverUrl = uploadResult.getSecureUrl();
        } else if (request.getCoverImageUrl() != null) {
            coverUrl = request.getCoverImageUrl();
        }

        Podcast podcast = Podcast.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .coverImageUrl(coverUrl)
                .hostId(request.getHostId())
                .categories(request.getCategories() != null ? request.getCategories() : new ArrayList<>())
                .episodeIds(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Podcast saved = podcastRepository.save(podcast);
        return convertToDto(saved);
    }

    @Override
    public PodcastDto getPodcastById(String id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Podcast not found with id: " + id));
        
        PodcastDto dto = convertToDto(podcast);
        
        // Populate nested episode details
        List<Episode> episodes = episodeRepository.findByPodcastId(id);
        dto.setEpisodes(episodes.stream().map(this::convertEpisodeToDto).collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public List<PodcastDto> getAllPodcasts() {
        return podcastRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PodcastDto updatePodcast(String id, PodcastRequest request, MultipartFile coverImageFile) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Podcast not found with id: " + id));

        if (request.getTitle() != null) {
            podcast.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            podcast.setDescription(request.getDescription());
        }
        if (request.getCategories() != null) {
            podcast.setCategories(request.getCategories());
        }
        if (request.getHostId() != null) {
            podcast.setHostId(request.getHostId());
        }

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            UploadResultDto uploadResult = cloudinaryService.uploadFile(coverImageFile, "podcast_covers");
            podcast.setCoverImageUrl(uploadResult.getSecureUrl());
        } else if (request.getCoverImageUrl() != null) {
            podcast.setCoverImageUrl(request.getCoverImageUrl());
        }

        podcast.setUpdatedAt(LocalDateTime.now());
        Podcast saved = podcastRepository.save(podcast);
        return convertToDto(saved);
    }

    @Override
    public void deletePodcast(String id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Podcast not found with id: " + id));

        // Delete all associated episodes
        List<Episode> episodes = episodeRepository.findByPodcastId(id);
        for (Episode episode : episodes) {
            deleteEpisode(episode.getId());
        }

        podcastRepository.delete(podcast);
    }

    // --- Episode CRUD ---

    @Override
    public EpisodeDto createEpisode(String podcastId, EpisodeRequest request, MultipartFile audioFile) {
        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new RuntimeException("Podcast not found with id: " + podcastId));

        String audioUrl = null;
        String publicId = null;

        if (audioFile != null && !audioFile.isEmpty()) {
            UploadResultDto uploadResult = cloudinaryService.uploadFile(audioFile, "podcast_audio");
            audioUrl = uploadResult.getSecureUrl();
            publicId = uploadResult.getPublicId();
        }

        String mediaType = "AUDIO";
        if (request.getMediaType() != null) {
            mediaType = request.getMediaType().toUpperCase();
        } else if (audioFile != null && audioFile.getContentType() != null) {
            if (audioFile.getContentType().startsWith("video/")) {
                mediaType = "VIDEO";
            }
        }

        Episode episode = Episode.builder()
                .podcastId(podcastId)
                .title(request.getTitle())
                .description(request.getDescription())
                .mediaUrl(audioUrl)
                .mediaPublicId(publicId)
                .durationMs(180000) // Mock default duration: 3 mins
                .playCount(0)
                .status(request.getStatus() != null ? request.getStatus() : "PUBLISHED")
                .mediaType(mediaType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Episode savedEpisode = episodeRepository.save(episode);

        // Add episode ID to the podcast's list
        if (podcast.getEpisodeIds() == null) {
            podcast.setEpisodeIds(new ArrayList<>());
        }
        podcast.getEpisodeIds().add(savedEpisode.getId());
        podcastRepository.save(podcast);

        return convertEpisodeToDto(savedEpisode);
    }

    @Override
    public EpisodeDto getEpisodeById(String id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episode not found with id: " + id));
        return convertEpisodeToDto(episode);
    }

    @Override
    public List<EpisodeDto> getEpisodesByPodcastId(String podcastId) {
        return episodeRepository.findByPodcastId(podcastId).stream()
                .map(this::convertEpisodeToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EpisodeDto updateEpisode(String episodeId, EpisodeRequest request, MultipartFile audioFile) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found with id: " + episodeId));

        if (request.getTitle() != null) {
            episode.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            episode.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            episode.setStatus(request.getStatus());
        }

        if (audioFile != null && !audioFile.isEmpty()) {
            // Delete old file if present
            if (episode.getMediaPublicId() != null) {
                try {
                    cloudinaryService.deleteFile(episode.getMediaPublicId());
                } catch (Exception e) {
                    System.err.println("Failed to delete old podcast media: " + e.getMessage());
                }
            }
            UploadResultDto uploadResult = cloudinaryService.uploadFile(audioFile, "podcast_audio");
            episode.setMediaUrl(uploadResult.getSecureUrl());
            episode.setMediaPublicId(uploadResult.getPublicId());
            
            // Auto detect media type
            if (audioFile.getContentType() != null) {
                if (audioFile.getContentType().startsWith("video/")) {
                    episode.setMediaType("VIDEO");
                } else {
                    episode.setMediaType("AUDIO");
                }
            }
        }

        if (request.getMediaType() != null) {
            episode.setMediaType(request.getMediaType().toUpperCase());
        }

        episode.setUpdatedAt(LocalDateTime.now());
        Episode saved = episodeRepository.save(episode);
        return convertEpisodeToDto(saved);
    }

    @Override
    public void deleteEpisode(String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found with id: " + episodeId));

        // Delete audio from Cloudinary
        if (episode.getMediaPublicId() != null) {
            try {
                cloudinaryService.deleteFile(episode.getMediaPublicId());
            } catch (Exception e) {
                System.err.println("Failed to delete podcast media from Cloudinary: " + e.getMessage());
            }
        }

        // Remove from Podcast list
        podcastRepository.findById(episode.getPodcastId()).ifPresent(podcast -> {
            if (podcast.getEpisodeIds() != null) {
                podcast.getEpisodeIds().remove(episodeId);
                podcastRepository.save(podcast);
            }
        });

        episodeRepository.delete(episode);
    }

    @Override
    public void incrementEpisodePlayCount(String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found with id: " + episodeId));
        episode.setPlayCount(episode.getPlayCount() + 1);
        episodeRepository.save(episode);
    }

    // --- Helper Mappers ---

    private PodcastDto convertToDto(Podcast podcast) {
        return PodcastDto.builder()
                .id(podcast.getId())
                .title(podcast.getTitle())
                .description(podcast.getDescription())
                .coverImageUrl(podcast.getCoverImageUrl())
                .hostId(podcast.getHostId())
                .categories(podcast.getCategories())
                .episodeIds(podcast.getEpisodeIds())
                .createdAt(podcast.getCreatedAt())
                .updatedAt(podcast.getUpdatedAt())
                .build();
    }

    private EpisodeDto convertEpisodeToDto(Episode episode) {
        return EpisodeDto.builder()
                .id(episode.getId())
                .podcastId(episode.getPodcastId())
                .title(episode.getTitle())
                .description(episode.getDescription())
                .mediaUrl(episode.getMediaUrl())
                .mediaPublicId(episode.getMediaPublicId())
                .durationMs(episode.getDurationMs())
                .playCount(episode.getPlayCount())
                .status(episode.getStatus())
                .mediaType(episode.getMediaType())
                .createdAt(episode.getCreatedAt())
                .updatedAt(episode.getUpdatedAt())
                .build();
    }
}
