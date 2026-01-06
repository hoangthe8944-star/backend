package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaylistDto {
    private String id;
    private String name;
    private String description;
    private String ownerId; 
    private boolean PublicPlaylist;
    private String type;  
    private List<String> tracks; // Danh sách ID bài hát
    private String coverImage;
}