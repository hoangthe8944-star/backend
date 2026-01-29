package com.example.beatboxcompany.Request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AlbumRequest {
    private String title;
    private LocalDate releaseDate;
    private String artistId;
    private List<TrackRequest> tracks;
}
