package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodcastDto {
    private String id;
    private String title;
    private String description;
    private String coverImageUrl;
    private String hostId;
    private List<String> categories;
    private List<String> episodeIds;
    private List<EpisodeDto> episodes; // Nested detailed list
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
