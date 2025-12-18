package com.example.beatboxcompany.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDto {
    private String id;
    private String name;
    private String bio;
    private String avatarUrl;
    private long followerCount;
    // Lưu ý: KHÔNG trả về userId cho người dùng công cộng
}