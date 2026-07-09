package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.PlaybackCheckRequest;
import com.example.beatboxcompany.Dto.PlaybackDecisionResponse;
import com.example.beatboxcompany.Service.PlaybackDecisionService;
import com.example.beatboxcompany.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playback")
@PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
@Slf4j
public class PlaybackController {

    private final PlaybackDecisionService playbackDecisionService;
    private final UserService userService;

    public PlaybackController(PlaybackDecisionService playbackDecisionService, UserService userService) {
        this.playbackDecisionService = playbackDecisionService;
        this.userService = userService;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserIdByEmail(email);
    }

    @PostMapping("/check")
    public ResponseEntity<PlaybackDecisionResponse> evaluatePlayback(@RequestBody PlaybackCheckRequest request) {
        String userId = getCurrentUserId();
        PlaybackDecisionResponse decision = playbackDecisionService.evaluatePlayback(userId, request);
        return ResponseEntity.ok(decision);
    }
}
