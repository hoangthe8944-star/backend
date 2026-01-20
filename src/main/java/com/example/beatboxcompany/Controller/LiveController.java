package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Entity.LiveRoom;
import com.example.beatboxcompany.Repository.LiveRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/live")
public class LiveController {

    @Autowired
    private LiveRoomRepository repository;

    @PostMapping("/start")
    public ResponseEntity<?> startLive(@RequestBody LiveRoom room) {
        room.setLive(true);
        room.setStartedAt(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(room));
    }

    @PostMapping("/end/{roomId}")
    public ResponseEntity<?> endLive(@PathVariable String roomId) {
        repository.findById(roomId).ifPresent(room -> {
            room.setLive(false);
            repository.save(room);
        });
        return ResponseEntity.ok("Live ended");
    }

    @GetMapping("/active")
    public List<LiveRoom> getActiveRooms() {
        return repository.findByIsLiveTrue();
    }
}