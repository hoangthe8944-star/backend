package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.AdvertisementDto;
import com.example.beatboxcompany.Entity.Advertisement;
import com.example.beatboxcompany.Repository.AdvertisementRepository;
import com.example.beatboxcompany.Service.AdvertisementService;
import com.example.beatboxcompany.Exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final Random random = new Random();

    public AdvertisementServiceImpl(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    private void initializeDefaultAds() {
        if (advertisementRepository.count() == 0) {
            log.info("Initializing default advertisements...");

            // 1. MP4 Video Ad
            Advertisement videoAd = new Advertisement();
            videoAd.setTitle("Nâng cấp lên Spotify Premium - Trải nghiệm âm nhạc không quảng cáo");
            videoAd.setPartnerName("Spotify Vietnam");
            videoAd.setAdType("VIDEO");
            videoAd.setMediaUrl("https://res.cloudinary.com/div8wpzfe/video/upload/v1700000000/ads/premium_ad.mp4");
            videoAd.setTargetUrl("https://spotify.com/vn-vi/premium");
            videoAd.setDuration(900);
            videoAd.setActive(true);
            
            // Set alias fields
            videoAd.setVideoUrl(videoAd.getMediaUrl());
            videoAd.setMp4(videoAd.getMediaUrl());
            
            advertisementRepository.save(videoAd);

            // 2. MP3 + Image Ad
            Advertisement audioImageAd = new Advertisement();
            audioImageAd.setTitle("Mua sắm thả ga tại Shopee - Siêu Sale 11.11");
            audioImageAd.setPartnerName("Shopee Vietnam");
            audioImageAd.setAdType("AUDIO_IMAGE");
            audioImageAd.setAudioUrl("https://res.cloudinary.com/div8wpzfe/video/upload/v1700000000/ads/shopee_jingle.mp3");
            audioImageAd.setImageUrl("https://res.cloudinary.com/div8wpzfe/image/upload/v1700000000/ads/shopee_banner.png");
            audioImageAd.setTargetUrl("https://shopee.vn");
            audioImageAd.setDuration(10);
            audioImageAd.setActive(true);
            
            // Set alias fields
            audioImageAd.setAudio(audioImageAd.getAudioUrl());
            audioImageAd.setImage(audioImageAd.getImageUrl());
            
            advertisementRepository.save(audioImageAd);
        }
    }

    private AdvertisementDto mapToDto(Advertisement ad) {
        AdvertisementDto dto = new AdvertisementDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setPartnerName(ad.getPartnerName());
        dto.setAdType(ad.getAdType());
        dto.setMediaUrl(ad.getMediaUrl());
        dto.setAudioUrl(ad.getAudioUrl());
        dto.setImageUrl(ad.getImageUrl());
        dto.setTargetUrl(ad.getTargetUrl());
        dto.setDuration(ad.getDuration());
        dto.setActive(ad.isActive());
        dto.setViews(ad.getViews());
        dto.setClicks(ad.getClicks());
        dto.setCreatedAt(ad.getCreatedAt());
        
        // Sync alias fields
        dto.setImage(ad.getImage() != null ? ad.getImage() : ad.getImageUrl());
        dto.setVideoUrl(ad.getVideoUrl() != null ? ad.getVideoUrl() : ad.getMediaUrl());
        dto.setMp4(ad.getMp4() != null ? ad.getMp4() : ad.getMediaUrl());
        dto.setAudio(ad.getAudio() != null ? ad.getAudio() : ad.getAudioUrl());
        
        return dto;
    }

    private Advertisement mapToEntity(AdvertisementDto dto) {
        Advertisement ad = new Advertisement();
        ad.setId(dto.getId());
        ad.setTitle(dto.getTitle());
        ad.setPartnerName(dto.getPartnerName());
        ad.setAdType(dto.getAdType());
        ad.setMediaUrl(dto.getMediaUrl());
        ad.setAudioUrl(dto.getAudioUrl());
        ad.setImageUrl(dto.getImageUrl());
        ad.setTargetUrl(dto.getTargetUrl());
        ad.setDuration(dto.getDuration());
        ad.setActive(dto.isActive());
        ad.setViews(dto.getViews());
        ad.setClicks(dto.getClicks());
        
        // Sync alias fields
        ad.setImage(dto.getImage() != null ? dto.getImage() : dto.getImageUrl());
        ad.setVideoUrl(dto.getVideoUrl() != null ? dto.getVideoUrl() : dto.getMediaUrl());
        ad.setMp4(dto.getMp4() != null ? dto.getMp4() : dto.getMediaUrl());
        ad.setAudio(dto.getAudio() != null ? dto.getAudio() : dto.getAudioUrl());
        
        return ad;
    }

    @Override
    public AdvertisementDto getRandomActiveAd() {
        initializeDefaultAds();
        List<Advertisement> activeAds = advertisementRepository.findByIsActive(true);
        if (activeAds.isEmpty()) {
            return null;
        }
        int index = random.nextInt(activeAds.size());
        return mapToDto(activeAds.get(index));
    }

    @Override
    public void trackView(String adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement", "id", adId));
        ad.setViews(ad.getViews() + 1);
        advertisementRepository.save(ad);
        log.info("Tracked view for ad id: {}", adId);
    }

    @Override
    public void trackClick(String adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement", "id", adId));
        ad.setClicks(ad.getClicks() + 1);
        advertisementRepository.save(ad);
        log.info("Tracked click for ad id: {}", adId);
    }

    @Override
    public AdvertisementDto createAd(AdvertisementDto dto) {
        Advertisement ad = mapToEntity(dto);
        ad.setCreatedAt(LocalDateTime.now());
        Advertisement saved = advertisementRepository.save(ad);
        log.info("Created new advertisement campaign: {}", saved.getTitle());
        return mapToDto(saved);
    }

    @Override
    public List<AdvertisementDto> getAllAds() {
        initializeDefaultAds();
        return advertisementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAd(String id) {
        if (!advertisementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Advertisement", "id", id);
        }
        advertisementRepository.deleteById(id);
        log.info("Deleted advertisement campaign id: {}", id);
    }
}
