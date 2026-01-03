package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;

@Data
public class LyricsDto {
    private String track;
    private String artist;
    private List<LyricLineDto> lyrics;
}
