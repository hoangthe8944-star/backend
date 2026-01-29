// package com.example.beatboxcompany.Controller;

// import com.example.beatboxcompany.Service.SpotifyService;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/admin/spotify")
// // @PreAuthorize("hasRole('ADMIN')") // Uncomment dòng này nếu muốn bảo mật chỉ Admin mới được tìm
// public class SpotifyController {

//     private final SpotifyService spotifyService;

//     public SpotifyController(SpotifyService spotifyService) {
//         this.spotifyService = spotifyService;
//     }

//     /**
//      * API: GET /api/admin/spotify/search?keyword=TênBàiHát
//      * Frontend gọi API này -> Backend gọi Spotify -> Trả về kết quả
//      */
//     @GetMapping("/search")
//     public ResponseEntity<Object> searchFromSpotify(@RequestParam String keyword) {
//         if (keyword == null || keyword.trim().isEmpty()) {
//             return ResponseEntity.badRequest().body("Vui lòng nhập từ khóa tìm kiếm");
//         }
//         return ResponseEntity.ok(spotifyService.searchTracks(keyword));
//     }
    
// }