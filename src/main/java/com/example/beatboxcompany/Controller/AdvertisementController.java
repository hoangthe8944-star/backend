package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.AdvertisementDto;
import com.example.beatboxcompany.Service.AdvertisementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/api/public/ads/random")
    public ResponseEntity<AdvertisementDto> getRandomActiveAd() {
        AdvertisementDto ad = advertisementService.getRandomActiveAd();
        if (ad == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ad);
    }

    @PostMapping("/api/public/ads/{id}/view")
    public ResponseEntity<Void> trackView(@PathVariable String id) {
        advertisementService.trackView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/public/ads/{id}/click")
    public ResponseEntity<Void> trackClick(@PathVariable String id) {
        advertisementService.trackClick(id);
        return ResponseEntity.ok().build();
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/api/admin/ads")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AdvertisementDto>> getAllAds() {
        return ResponseEntity.ok(advertisementService.getAllAds());
    }

    @PostMapping("/api/admin/ads")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdvertisementDto> createAd(@RequestBody AdvertisementDto dto) {
        return ResponseEntity.ok(advertisementService.createAd(dto));
    }

    @DeleteMapping("/api/admin/ads/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAd(@PathVariable String id) {
        advertisementService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }
}
