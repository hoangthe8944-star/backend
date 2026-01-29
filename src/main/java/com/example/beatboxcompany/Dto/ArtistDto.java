package com.example.beatboxcompany.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDto {
    private String id;
    private String name;
    private String bio;
    private List<String> genres;
    private String avatarUrl;
    private String coverImageUrl;
    private long followerCount;
    // Lưu ý: KHÔNG trả về userId cho người dùng công cộng
}