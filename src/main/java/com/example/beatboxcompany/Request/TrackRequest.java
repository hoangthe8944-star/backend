package com.example.beatboxcompany.Request;   // nhớ sửa theo đúng package của bạn

import lombok.Data;

@Data
public class TrackRequest {

    private String title;

    // trackNumber có thể để null → backend tự generate
    private Integer trackNumber;

    private Long durationMs;

    // mặc định không explicit
    private Boolean isExplicit = false;
}
