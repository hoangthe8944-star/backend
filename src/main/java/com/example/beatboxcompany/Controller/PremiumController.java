package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.PremiumStatusDto;
import com.example.beatboxcompany.Request.PurchasePremiumRequest;
import com.example.beatboxcompany.Service.PremiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumService premiumService;

    /**
     * GET /api/users/:userId/premium - Kiểm tra trạng thái VIP
     */
    @GetMapping("/users/{userId}/premium")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<PremiumStatusDto> getPremiumStatus(@PathVariable String userId) {
        // Kiểm tra quyền: user chỉ có thể xem premium status của chính họ (trừ ADMIN)
        validateUserAccess(userId);

        PremiumStatusDto status = premiumService.getPremiumStatus(userId);
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/premium/purchase - Mua gói VIP
     */
    @PostMapping("/premium/purchase")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<PremiumStatusDto> purchasePremium(@RequestBody PurchasePremiumRequest request) {
        // Lấy userId từ token hiện tại
        String userId = getCurrentUserId();

        PremiumStatusDto status = premiumService.purchasePremium(userId, request);
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    /**
     * DELETE /api/users/:userId/premium - Hủy VIP
     */
    @DeleteMapping("/users/{userId}/premium")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<Void> cancelPremium(@PathVariable String userId) {
        // Kiểm tra quyền: user chỉ có thể hủy premium của chính họ (trừ ADMIN)
        validateUserAccess(userId);

        premiumService.cancelPremium(userId);
        return ResponseEntity.noContent().build();
    }

    // ===== Helper Methods =====

    /**
     * Lấy userId của user hiện tại từ JWT token
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // TODO: Implement logic để lấy userId từ email
        // Tạm thời return email, bạn cần implement getUserIdByEmail() trong UserService
        // và inject UserService vào controller này
        throw new RuntimeException("Cần implement getUserIdByEmail() trong UserService");
    }

    /**
     * Validate rằng user chỉ có thể truy cập premium của chính họ
     * (trừ khi có role ADMIN)
     */
    private void validateUserAccess(String requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu là ADMIN thì cho phép
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // ADMIN có thể truy cập premium của bất kỳ user nào
        }

        // Nếu không phải ADMIN, kiểm tra xem có phải premium của chính họ không
        String currentUserId = getCurrentUserId();
        if (!currentUserId.equals(requestedUserId)) {
            throw new RuntimeException("Bạn không có quyền truy cập thông tin premium của người dùng khác");
        }
    }
}
