package com.example.beatboxcompany.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "playlists") 
public class Playlist {

    @Id
    private String id;
    
    private String name;
    private String description;
    
    // ID của người tạo/quản lý Playlist (liên kết với User Entity)
    private String ownerId; 
    
    // Playlist có công khai hay không (cho PublicController)
    private boolean isPublic = false; 
    
    // Danh sách ID các bài hát trong Playlist
    private List<String> tracks = new ArrayList<>(); 
}