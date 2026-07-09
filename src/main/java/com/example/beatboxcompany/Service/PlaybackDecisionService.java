package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.PlaybackCheckRequest;
import com.example.beatboxcompany.Dto.PlaybackDecisionResponse;

public interface PlaybackDecisionService {
    PlaybackDecisionResponse evaluatePlayback(String userId, PlaybackCheckRequest request);
}
