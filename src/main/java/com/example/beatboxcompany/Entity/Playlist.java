package com.example.beatboxcompany.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Document(collection = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    private String id;

    private String name;
    private String description;

    private String ownerId;
    private boolean publicPlaylist;

    private String type; // user | editorial | system

    // Lưu trackId trực tiếp
    private List<String> tracks = new ArrayList<>();
    private String coverImage; 
}
