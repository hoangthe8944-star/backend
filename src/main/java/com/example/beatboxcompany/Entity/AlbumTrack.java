package com.example.beatboxcompany.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "album_tracks")
public class AlbumTrack {

    @Id
    private String id; // trong Mongo dùng String cho ObjectId

    private String albumId;     // ID của album
    private String songId;      // ID của bài hát
    private Integer trackNumber; // Thứ tự track trong album
    private Integer discNumber = 1; // Disc (mặc định 1)
}
