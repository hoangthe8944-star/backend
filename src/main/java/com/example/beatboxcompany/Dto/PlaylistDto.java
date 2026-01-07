package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.util.List;
import java.util.Date;

@Data
public class PlaylistDto {
    private String id;
    private String name;
    private String description;
    private String ownerId; 
    private String ownerName; // THÊM: Để hiện "Tạo bởi [Tên]" ở giao diện
    
    private boolean publicPlaylist;
    private String type;  
    
    private List<String> tracks; // Vẫn giữ mảng ID để tham chiếu
    
    // THÊM: Danh sách chi tiết bài hát (Sử dụng SongDto bạn đã có)
    private List<SongDto> songDetails; 

    private String coverImage;
    private int songCount;    // THÊM: Để hiện "15 bài hát" mà không cần đếm mảng
    private Date updatedAt;   // THÊM: Để hiện "Cập nhật ngày ..."
}