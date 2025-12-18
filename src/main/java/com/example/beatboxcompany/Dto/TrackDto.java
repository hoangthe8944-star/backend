package com.example.beatboxcompany.Dto;

import lombok.Data;

@Data
public class TrackDto {
    private String songId;
    private String title;
    private Long durationMs;
    private String durationText; // "03:45"
    private Boolean isExplicit;
    private String streamUrl;
}