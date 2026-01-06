package com.example.beatboxcompany.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistRequest {

    private String name;
    private String description;

    // Loại playlist: user | editorial | system
    private String type;

    // Playlist có công khai hay không
    @JsonProperty("isPublic")
    private Boolean isPublic;

    // Danh sách trackId, dùng List<String> cho đơn giản
    private List<String> tracks = new ArrayList<>();
    private String coverImage;
}
