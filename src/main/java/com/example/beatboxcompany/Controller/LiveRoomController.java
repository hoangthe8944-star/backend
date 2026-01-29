package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Entity.LiveRoom;
import com.example.beatboxcompany.Service.LiveRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*")
public class LiveRoomController {

    private final LiveRoomService service;

    @PostMapping("/start")
    public ResponseEntity<LiveRoom> start(
            @RequestParam String hostId,
            @RequestParam String hostName,
            @RequestParam String title) {
        return ResponseEntity.ok(service.startLive(hostId, hostName, title));
    }

    @PostMapping("/end/{roomId}")
    public ResponseEntity<?> end(@PathVariable String roomId) {
        service.endLive(roomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<LiveRoom>> active() {
        return ResponseEntity.ok(service.getActiveRooms());
    }

    @GetMapping("/zego-token")
    public ResponseEntity<?> getToken(@RequestParam String userId) {
        String token = service.generateZegoToken(userId);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
