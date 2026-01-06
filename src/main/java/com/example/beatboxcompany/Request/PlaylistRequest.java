package com.example.beatboxcompany.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistRequest {

    private String name;
    private String description;

    // Loại playlist: user | editorial | system
    private String type;

    // Playlist có công khai hay không
    private Boolean isPublic = false;

    // Danh sách trackId, dùng List<String> cho đơn giản
    private List<String> tracks = new ArrayList<>();
}
