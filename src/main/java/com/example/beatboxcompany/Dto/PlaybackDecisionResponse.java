package com.example.beatboxcompany.Dto;

import lombok.Data;

@Data
public class PlaybackDecisionResponse {
    // "PLAY_SONG" or "PLAY_AD"
    private String action; 
    private AdvertisementDto ad;
    private long nextAdInSeconds;
    private String reason;
}
