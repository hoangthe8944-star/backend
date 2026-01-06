package com.example.beatboxcompany.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTrack {

    private String trackId;
    private int position;
    private long addedAt;
    private String addedBy; // userId hoặc "system"
}
