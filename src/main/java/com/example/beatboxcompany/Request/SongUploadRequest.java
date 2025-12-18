package com.example.beatboxcompany.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // Cần thiết cho Jackson (thư viện xử lý JSON)
@AllArgsConstructor
public class SongUploadRequest {
    
    private String title;
    
    // Lưu ý: Trường này thường được Backend tự lấy từ Token của người đăng nhập
    // Nhưng nếu bạn gửi từ Frontend thì giữ lại cũng được.
    private String artistId; 
    
    private String albumId;   // Có thể null nếu là Single
    
    // NÊN ĐỔI: int -> Integer để chấp nhận giá trị null (tránh lỗi 0 mặc định)
    private Integer duration; 
    
    private String streamUrl;
    private String coverUrl;
    
    private List<String> genre;
    
    // Mặc định là false
    private Boolean isExplicit = false;
}