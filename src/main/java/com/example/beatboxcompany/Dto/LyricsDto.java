package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;

@Data
public class LyricsDto {
    private List<LyricsLine> lines;
    private boolean synced; // true = có timestamp
}
