package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.LyricsDto;
import com.example.beatboxcompany.Service.LyricsService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lyrics") // Phải khớp 100% với Frontend gọi
@CrossOrigin(origins = "*")
public class LyricsController {

 @Autowired
    private LyricsService lyricsService;

    @GetMapping
    public ResponseEntity<LyricsDto> getLyrics(
            @RequestParam String track,
            @RequestParam String artist) {
        
        try {
            LyricsDto result = lyricsService.getLyrics(track, artist);
            if (result != null) {
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            // Bất kể lỗi gì xảy ra bên trong Service, cũng không cho phép văng lỗi 500
            System.err.println("Fatal Controller Error: " + e.getMessage());
        }

        // Thay vì trả về 500 hoặc 404, trả về một Object rỗng với mã 200
        // Cách này giúp Axios ở Frontend không bị văng vào khối .catch()
        return ResponseEntity.ok(new LyricsDto());
    }
}