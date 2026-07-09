package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {
    private String id;
    private String podcastId;
    private String title;
    private String description;
    private String mediaUrl;
    private String mediaPublicId;
    private long durationMs;
    private long playCount;
    private String status;
    private String mediaType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
