package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.HistoryDto;
import java.util.List;

public interface HistoryService {
    void recordSongPlay(String userId, String songId);
    List<HistoryDto> getUserHistory(String userId);
    void clearHistory(String userId);
}