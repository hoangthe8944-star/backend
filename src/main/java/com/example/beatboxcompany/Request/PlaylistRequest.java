package com.example.beatboxcompany.Request;

import lombok.Data;
import java.util.List;

@Data
public class PlaylistRequest {
    private String name;
    private String description;
    private Boolean isPublic; // Có thể null khi tạo
    private List<String> tracks; // Có thể có hoặc không khi tạo
}