package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.AdvertisementDto;
import java.util.List;

public interface AdvertisementService {
    AdvertisementDto getRandomActiveAd();
    void trackView(String adId);
    void trackClick(String adId);
    AdvertisementDto createAd(AdvertisementDto dto);
    List<AdvertisementDto> getAllAds();
    void deleteAd(String id);
}
