package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.PremiumStatusDto;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Request.PurchasePremiumRequest;
import com.example.beatboxcompany.Service.PremiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PremiumServiceImpl implements PremiumService {

    private final UserRepository userRepository;

    @Override
    public PremiumStatusDto getPremiumStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Kiểm tra và cập nhật nếu premium đã hết hạn
        if (user.getIsPremium() && user.getPremiumExpiry() != null) {
            if (user.getPremiumExpiry().isBefore(LocalDateTime.now())) {
                // Tự động hủy premium nếu đã hết hạn
                user.setIsPremium(false);
                user.setPremiumExpiry(null);
                user.setPremiumType(null);
                userRepository.save(user);
            }
        }

        // Tính số ngày còn lại
        Long daysRemaining = null;
        if (user.getIsPremium() && user.getPremiumExpiry() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getPremiumExpiry());
            if (daysRemaining < 0) {
                daysRemaining = 0L;
            }
        }

        return PremiumStatusDto.builder()
                .isPremium(user.getIsPremium())
                .premiumType(user.getPremiumType())
                .premiumExpiry(user.getPremiumExpiry())
                .daysRemaining(daysRemaining)
                .build();
    }

    @Override
    public PremiumStatusDto purchasePremium(String userId, PurchasePremiumRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Validate request
        if (request.getPremiumType() == null || request.getPremiumType().trim().isEmpty()) {
            throw new RuntimeException("Loại gói premium là bắt buộc");
        }

        if (!request.getPremiumType().equals("monthly") && !request.getPremiumType().equals("yearly")) {
            throw new RuntimeException("Loại gói premium không hợp lệ. Chỉ chấp nhận 'monthly' hoặc 'yearly'");
        }

        // TODO: Tích hợp payment gateway ở đây
        // Ví dụ: processPayment(request.getPaymentToken(), request.getPaymentMethod());
        // Nếu thanh toán thất bại, throw exception

        // Tính ngày hết hạn
        LocalDateTime expiryDate;
        LocalDateTime now = LocalDateTime.now();

        // Nếu user đang có premium, gia hạn từ ngày hết hạn cũ thay vì từ bây giờ
        LocalDateTime startDate = now;
        if (user.getIsPremium() && user.getPremiumExpiry() != null && user.getPremiumExpiry().isAfter(now)) {
            startDate = user.getPremiumExpiry();
        }

        if (request.getPremiumType().equals("monthly")) {
            expiryDate = startDate.plusDays(30);
        } else { // yearly
            expiryDate = startDate.plusDays(365);
        }

        // Cập nhật trạng thái premium
        user.setIsPremium(true);
        user.setPremiumExpiry(expiryDate);
        user.setPremiumType(request.getPremiumType());

        User savedUser = userRepository.save(user);

        // Tính số ngày còn lại
        Long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), savedUser.getPremiumExpiry());

        return PremiumStatusDto.builder()
                .isPremium(savedUser.getIsPremium())
                .premiumType(savedUser.getPremiumType())
                .premiumExpiry(savedUser.getPremiumExpiry())
                .daysRemaining(daysRemaining)
                .build();
    }

    @Override
    public void cancelPremium(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Hủy premium
        user.setIsPremium(false);
        user.setPremiumExpiry(null);
        user.setPremiumType(null);

        userRepository.save(user);
    }
}
