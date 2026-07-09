package com.example.beatboxcompany.Dto;

import lombok.Data;

@Data
public class PlaybackCheckRequest {
    private String currentSongId;
    private boolean isPlaying;
    private int songProgressSeconds;
    private int songDurationSeconds;
}
