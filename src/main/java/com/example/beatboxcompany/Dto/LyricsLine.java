package com.example.beatboxcompany.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LyricsLine {
    private Double time; // giây
    private String text;
}
