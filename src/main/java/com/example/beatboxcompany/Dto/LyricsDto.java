package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;

@Data
public class LyricsDto {
    private String lyrics; // text thường
    private List<LyricsLine> lines;
    private String source;
}
