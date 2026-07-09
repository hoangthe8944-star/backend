package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.PremiumStatusResponse;
import com.example.beatboxcompany.Dto.SubscriptionDto;
import com.example.beatboxcompany.Service.PremiumService;
import com.example.beatboxcompany.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premium")
@PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
public class PremiumController {

    private final PremiumService premiumService;
    private final UserService userService;

    public PremiumController(PremiumService premiumService, UserService userService) {
        this.premiumService = premiumService;
        this.userService = userService;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserIdByEmail(email);
    }

    @GetMapping("/packages")
    public ResponseEntity<List<SubscriptionDto>> getAvailablePackages() {
        return ResponseEntity.ok(premiumService.getAvailablePackages());
    }

    @GetMapping("/status")
    public ResponseEntity<PremiumStatusResponse> getPremiumStatus() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(premiumService.getPremiumStatus(userId));
    }

    @PostMapping("/subscribe/{packageId}")
    public ResponseEntity<PremiumStatusResponse> subscribe(@PathVariable String packageId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(premiumService.subscribe(userId, packageId));
    }

    @PostMapping("/cancel")
    public ResponseEntity<PremiumStatusResponse> cancelSubscription() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(premiumService.cancelSubscription(userId));
    }
}
