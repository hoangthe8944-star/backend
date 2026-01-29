package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Entity.LiveRoom;
import com.example.beatboxcompany.Repository.LiveRoomRepository;
import com.example.beatboxcompany.utils.ZegoTokenGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LiveRoomService {

    private final LiveRoomRepository roomRepository;

    @Value("${zego.app.id}")
    private long appId;

    @Value("${zego.server.secret}")
    private String serverSecret;

    public String generateZegoToken(String userId) {
        return ZegoTokenGenerator.generateToken(
                appId,
                serverSecret,
                userId,
                3600);
    }

    public LiveRoom startLive(String hostId, String hostName, String title) {

        roomRepository.findByHostIdAndStatus(hostId, "LIVE")
                .ifPresent(r -> {
                    r.setStatus("ENDED");
                    roomRepository.save(r);
                });

        LiveRoom room = LiveRoom.builder()
                .roomId("room_" + hostId + "_" + System.currentTimeMillis())
                .hostId(hostId)
                .hostName(hostName)
                .title(title)
                .status("LIVE")
                .startedAt(new Date())
                .build();

        return roomRepository.save(room);
    }

    public List<LiveRoom> getActiveRooms() {
        return roomRepository.findByStatus("LIVE");
    }

    public void endLive(String roomId) {
        roomRepository.findById(roomId).ifPresent(r -> {
            r.setStatus("ENDED");
            roomRepository.save(r);
        });
    }
}
