package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.EpisodeDto;
import com.example.beatboxcompany.Dto.PodcastDto;
import com.example.beatboxcompany.Request.EpisodeRequest;
import com.example.beatboxcompany.Request.PodcastRequest;
import com.example.beatboxcompany.Service.PodcastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    private final PodcastService podcastService;

    public PodcastController(PodcastService podcastService) {
        this.podcastService = podcastService;
    }

    // --- Podcast Endpoints ---

    @PostMapping
    public ResponseEntity<PodcastDto> createPodcast(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("hostId") String hostId,
            @RequestParam(value = "categories", required = false) List<String> categories,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        PodcastRequest request = new PodcastRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setHostId(hostId);
        request.setCategories(categories);

        PodcastDto created = podcastService.createPodcast(request, coverImage);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PodcastDto> getPodcastById(@PathVariable String id) {
        return ResponseEntity.ok(podcastService.getPodcastById(id));
    }

    @GetMapping
    public ResponseEntity<List<PodcastDto>> getAllPodcasts() {
        return ResponseEntity.ok(podcastService.getAllPodcasts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PodcastDto> updatePodcast(
            @PathVariable String id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hostId", required = false) String hostId,
            @RequestParam(value = "categories", required = false) List<String> categories,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        PodcastRequest request = new PodcastRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setHostId(hostId);
        request.setCategories(categories);

        PodcastDto updated = podcastService.updatePodcast(id, request, coverImage);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePodcast(@PathVariable String id) {
        podcastService.deletePodcast(id);
        return ResponseEntity.ok().build();
    }

    // --- Episode Endpoints ---

    @PostMapping("/{id}/episodes")
    public ResponseEntity<EpisodeDto> createEpisode(
            @PathVariable("id") String podcastId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "status", defaultValue = "PUBLISHED") String status,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @RequestParam("audioFile") MultipartFile audioFile) {

        EpisodeRequest request = new EpisodeRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setStatus(status);
        request.setMediaType(mediaType);

        EpisodeDto created = podcastService.createEpisode(podcastId, request, audioFile);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<EpisodeDto> getEpisodeById(@PathVariable String episodeId) {
        return ResponseEntity.ok(podcastService.getEpisodeById(episodeId));
    }

    @GetMapping("/{id}/episodes")
    public ResponseEntity<List<EpisodeDto>> getEpisodesByPodcastId(@PathVariable("id") String podcastId) {
        return ResponseEntity.ok(podcastService.getEpisodesByPodcastId(podcastId));
    }

    @PutMapping("/episodes/{episodeId}")
    public ResponseEntity<EpisodeDto> updateEpisode(
            @PathVariable String episodeId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile) {

        EpisodeRequest request = new EpisodeRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setStatus(status);
        request.setMediaType(mediaType);

        EpisodeDto updated = podcastService.updateEpisode(episodeId, request, audioFile);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/episodes/{episodeId}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable String episodeId) {
        podcastService.deleteEpisode(episodeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/episodes/{episodeId}/listen")
    public ResponseEntity<Void> listenEpisode(@PathVariable String episodeId) {
        podcastService.incrementEpisodePlayCount(episodeId);
        return ResponseEntity.ok().build();
    }
}
