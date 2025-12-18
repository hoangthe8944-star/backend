package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.UploadResultDto;
import com.example.beatboxcompany.Service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;

    public MediaController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    // Ví dụ về API tải lên ảnh bìa Album
    @PostMapping("/album-cover")
    public ResponseEntity<UploadResultDto> uploadAlbumCover(@RequestParam("file") MultipartFile file) {
        // Gọi Service và chỉ định thư mục lưu trữ
        UploadResultDto result = cloudinaryService.uploadFile(file, "beatbox_assets/album_covers");

        // *BƯỚC CẦN LÀM SAU ĐÓ:* // 1. Lấy publicId (result.getPublicId())
        // 2. Lưu publicId vào Entity Album trong MongoDB
        // 3. Trả về thông báo thành công cho client

        return ResponseEntity.ok(result);
    }
    
    // Ví dụ về API tải lên file nhạc/audio
    @PostMapping("/song-file")
    public ResponseEntity<UploadResultDto> uploadSongFile(@RequestParam("file") MultipartFile file) {
        // Lưu file nhạc vào thư mục "song_files"
        UploadResultDto result = cloudinaryService.uploadFile(file, "beatbox_assets/song_files");

        // *BƯỚC CẦN LÀM SAU ĐÓ:* // Lưu publicId (result.getPublicId()) vào Entity Song trong MongoDB

        return ResponseEntity.ok(result);
    }

    // Ví dụ về API xóa file
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("publicId") String publicId) {
        // Giả sử publicId được truyền từ client sau khi xóa Entity khỏi MongoDB
        cloudinaryService.deleteFile(publicId);
        return ResponseEntity.ok("Đã xóa file thành công với Public ID: " + publicId);
    }
}