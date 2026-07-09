package com.example.beatboxcompany.Request;

import lombok.Data;
import java.util.List;

@Data
public class PodcastRequest {
    private String title;
    private String description;
    private String coverImageUrl;
    private String hostId;
    private List<String> categories;
}
