package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.PlaybackCheckRequest;
import com.example.beatboxcompany.Dto.PlaybackDecisionResponse;
import com.example.beatboxcompany.Dto.AdvertisementDto;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Service.AdvertisementService;
import com.example.beatboxcompany.Service.PlaybackDecisionService;
import com.example.beatboxcompany.Exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class PlaybackDecisionServiceImpl implements PlaybackDecisionService {

    private final UserRepository userRepository;
    private final AdvertisementService advertisementService;
    
    private static final long AD_INTERVAL_SECONDS = 300; // 15 minutes

    public PlaybackDecisionServiceImpl(UserRepository userRepository, AdvertisementService advertisementService) {
        this.userRepository = userRepository;
        this.advertisementService = advertisementService;
    }

    @Override
    public PlaybackDecisionResponse evaluatePlayback(String userId, PlaybackCheckRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        PlaybackDecisionResponse response = new PlaybackDecisionResponse();

        // 1. Kiểm tra tài khoản Premium
        boolean isPremium = user.getPremiumExpiresAt() != null 
                && user.getPremiumExpiresAt().isAfter(LocalDateTime.now());

        if (isPremium) {
            response.setAction("PLAY_SONG");
            response.setAd(null);
            response.setNextAdInSeconds(-1);
            response.setReason("Tài khoản Premium: Không có quảng cáo.");
            return response;
        }

        // 2. Khởi tạo mốc thời gian quảng cáo nếu rỗng
        if (user.getLastAdAt() == null) {
            user.setLastAdAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // 3. Tính toán thời gian đã trôi qua
        long elapsedSeconds = ChronoUnit.SECONDS.between(user.getLastAdAt(), LocalDateTime.now());

        if (elapsedSeconds >= AD_INTERVAL_SECONDS) {
            // Đã đến hạn quảng cáo (> 15 phút)
            // Kiểm tra xem bài hát có đang phát giữa chừng không
            boolean isSongActive = request.isPlaying() 
                    && request.getSongDurationSeconds() > 0 
                    && request.getSongProgressSeconds() < (request.getSongDurationSeconds() - 2); // Trừ 2 giây buffer trước khi kết thúc hoàn toàn

            if (isSongActive) {
                // Bài hát vẫn đang phát -> Cho phép hoàn thành bài hát, KHÔNG ngắt nhạc
                response.setAction("PLAY_SONG");
                response.setAd(null);
                response.setNextAdInSeconds(0); // Quảng cáo sẽ phát ngay khi bài hát này kết thúc
                response.setReason("Đã đến thời gian quảng cáo, nhưng hoãn lại để bài hát hiện tại phát xong.");
                log.info("Ad is due for user {}, but delayed until current song finishing (Progress: {}/{}s)", 
                        user.getEmail(), request.getSongProgressSeconds(), request.getSongDurationSeconds());
            } else {
                // Bài hát đã kết thúc hoặc không phát nhạc -> Phát quảng cáo ngay
                AdvertisementDto ad = advertisementService.getRandomActiveAd();
                
                // Cập nhật mốc thời gian quảng cáo gần nhất
                user.setLastAdAt(LocalDateTime.now());
                userRepository.save(user);

                response.setAction("PLAY_AD");
                response.setAd(ad);
                response.setNextAdInSeconds(AD_INTERVAL_SECONDS);
                response.setReason("Đã đến hạn quảng cáo 15 phút. Tiến hành phát quảng cáo.");
                log.info("Triggering advertisement playback for free user {}", user.getEmail());
            }
        } else {
            // Chưa đến hạn quảng cáo
            long remainingSeconds = AD_INTERVAL_SECONDS - elapsedSeconds;
            response.setAction("PLAY_SONG");
            response.setAd(null);
            response.setNextAdInSeconds(remainingSeconds);
            response.setReason("Chưa đến hạn quảng cáo. Thời gian còn lại: " + remainingSeconds + " giây.");
        }

        return response;
    }
}
