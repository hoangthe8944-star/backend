package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Service.PremiumService;
import com.example.beatboxcompany.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class MomoController {

    private final PremiumService premiumService;
    private final UserService userService;

    public MomoController(PremiumService premiumService, UserService userService) {
        this.premiumService = premiumService;
        this.userService = userService;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserIdByEmail(email);
    }

    @PostMapping("/api/momo/payment/{packageId}")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<Map<String, String>> initiateMomoPayment(@PathVariable String packageId) throws Exception {
        String userId = getCurrentUserId();
        log.info("Initiating MoMo payment for user: {} and package: {}", userId, packageId);
        
        String payUrl = premiumService.initiateMomoPayment(userId, packageId);
        Map<String, String> response = new HashMap<>();
        response.put("payUrl", payUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/public/momo/ipn")
    public ResponseEntity<Void> handleMomoIPN(@RequestBody Map<String, String> ipnParams) {
        log.info("Received MoMo IPN Callback at dedicated MomoController");
        try {
            premiumService.processMomoIPN(ipnParams);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to process MoMo IPN Callback", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
