package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.HistoryDto;
import com.example.beatboxcompany.Dto.SongDto;
import com.example.beatboxcompany.Entity.History;
import com.example.beatboxcompany.Repository.HistoryRepository;
import com.example.beatboxcompany.Service.HistoryService;
import com.example.beatboxcompany.Service.SongService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final SongService songService;

    public HistoryServiceImpl(HistoryRepository historyRepository, SongService songService) {
        this.historyRepository = historyRepository;
        this.songService = songService;
    }

    @Override
    public void recordSongPlay(String userId, String songId) {
        if (userId == null || songId == null) return;

        // Kiểm tra xem đã có trong lịch sử chưa
        historyRepository.findByUserIdAndSongId(userId, songId)
            .ifPresentOrElse(
                history -> {
                    // Nếu có rồi: Cập nhật thời gian nghe mới nhất
                    history.setPlayedAt(LocalDateTime.now());
                    historyRepository.save(history);
                },
                () -> {
                    // Nếu chưa có: Tạo mới
                    History newHistory = new History();
                    newHistory.setUserId(userId);
                    newHistory.setSongId(songId);
                    newHistory.setPlayedAt(LocalDateTime.now());
                    historyRepository.save(newHistory);
                }
            );
    }

    @Override
    public List<HistoryDto> getUserHistory(String userId) {
        // Lấy danh sách history của user, giới hạn 20 bài gần nhất
        return historyRepository.findByUserIdOrderByPlayedAtDesc(userId).stream()
                .limit(20) 
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void clearHistory(String userId) {
        List<History> userHistory = historyRepository.findByUserIdOrderByPlayedAtDesc(userId);
        historyRepository.deleteAll(userHistory);
    }

    // Hàm bổ trợ nạp thông tin bài hát vào DTO (tránh lỗi 404 ảnh trên FE)
    private HistoryDto convertToDto(History history) {
        HistoryDto dto = new HistoryDto();
        dto.setId(history.getId());
        dto.setUserId(history.getUserId());
        dto.setSongId(history.getSongId());
        dto.setPlayedAt(history.getPlayedAt());

        try {
            // Gọi songService để lấy title, coverUrl, artist...
            SongDto song = songService.getPublishedSongById(history.getSongId());
            dto.setSongDetails(song);
        } catch (Exception e) {
            dto.setSongDetails(null);
        }
        return dto;
    }
}