package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Dto.PremiumStatusResponse;
import com.example.beatboxcompany.Dto.SubscriptionDto;
import com.example.beatboxcompany.Entity.PaymentTransaction;
import com.example.beatboxcompany.Entity.Subscription;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.PaymentTransactionRepository;
import com.example.beatboxcompany.Repository.SubscriptionRepository;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Service.MomoService;
import com.example.beatboxcompany.Service.PremiumService;
import com.example.beatboxcompany.Exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PremiumServiceImpl implements PremiumService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MomoService momoService;

    public PremiumServiceImpl(
            UserRepository userRepository, 
            SubscriptionRepository subscriptionRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            MomoService momoService) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.momoService = momoService;
    }

    private void initializeDefaultPackages() {
        // 1. Mini
        Subscription mini = subscriptionRepository.findByName("Mini").orElse(new Subscription());
        mini.setName("Mini");
        mini.setDescription("Nghe nhạc không quảng cáo, Tua nhạc & Chuyển bài hát (Tối đa 30 lần/ngày), Chất lượng âm thanh chuẩn");
        mini.setPrice(new BigDecimal("19000"));
        mini.setCurrency("VND");
        mini.setDuration("P7D");
        mini.setFeatures(List.of("Nghe nhạc không quảng cáo", "Tua nhạc & Chuyển bài hát", "Tối đa 30 lần chuyển bài/ngày", "Chất lượng âm thanh chuẩn"));
        mini.setStatus("ACTIVE");
        subscriptionRepository.save(mini);

        // 2. Premium Cá nhân
        Subscription personal = subscriptionRepository.findByName("Premium Cá nhân").orElse(new Subscription());
        personal.setName("Premium Cá nhân");
        personal.setDescription("Nghe nhạc không quảng cáo, Chuyển bài không giới hạn, Mở khóa lời bài hát (Lyrics), Tăng tốc độ phát nhạc (Speedup), Âm thanh chất lượng cao");
        personal.setPrice(new BigDecimal("59000"));
        personal.setCurrency("VND");
        personal.setDuration("P30D");
        personal.setFeatures(List.of("Nghe nhạc không quảng cáo", "Chuyển bài không giới hạn", "Mở khóa lời bài hát (Lyrics)", "Tăng tốc độ phát nhạc (Speedup)", "Chất lượng âm thanh cao cấp"));
        personal.setStatus("ACTIVE");
        subscriptionRepository.save(personal);

        // 3. Premium Gia đình
        Subscription family = subscriptionRepository.findByName("Premium Gia đình").orElse(new Subscription());
        family.setName("Premium Gia đình");
        family.setDescription("Tối đa 5 tài khoản Premium, Tất cả quyền lợi Premium Cá nhân, Playlist gia đình chung, Kiểm soát nội dung cho trẻ em");
        family.setPrice(new BigDecimal("99000"));
        family.setCurrency("VND");
        family.setDuration("P30D");
        family.setFeatures(List.of("Tối đa 5 tài khoản Premium", "Đầy đủ quyền lợi Premium Cá nhân", "Xem Lyrics & Speedup nhạc", "Playlist gia đình chung", "Kiểm soát nội dung cho trẻ em"));
        family.setStatus("ACTIVE");
        subscriptionRepository.save(family);
    }

    private SubscriptionDto mapToDto(Subscription subscription) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(subscription.getId());
        dto.setName(subscription.getName());
        dto.setDescription(subscription.getDescription());
        dto.setPrice(subscription.getPrice());
        dto.setCurrency(subscription.getCurrency());
        dto.setDuration(subscription.getDuration());
        dto.setFeatures(subscription.getFeatures());
        dto.setStatus(subscription.getStatus());
        return dto;
    }

    @Override
    public List<SubscriptionDto> getAvailablePackages() {
        initializeDefaultPackages();
        return subscriptionRepository.findAll().stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PremiumStatusResponse getPremiumStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        PremiumStatusResponse response = new PremiumStatusResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCoins(user.getCoins() != null ? user.getCoins() : 0L);

        boolean isPremiumActive = user.getPremiumExpiresAt() != null 
                && user.getPremiumExpiresAt().isAfter(LocalDateTime.now());

        response.setPremium(isPremiumActive);
        
        if (isPremiumActive && user.getPremiumType() != null) {
            response.setPremiumType(user.getPremiumType());
            response.setPremiumExpiresAt(user.getPremiumExpiresAt());
            
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getPremiumExpiresAt());
            response.setDaysRemaining(days < 0 ? 0 : days);

            Optional<Subscription> subOpt = subscriptionRepository.findByName(user.getPremiumType());
            if (subOpt.isPresent()) {
                Subscription sub = subOpt.get();
                response.setPrice(sub.getPrice());
                response.setCurrency(sub.getCurrency());
                response.setDuration(sub.getDuration());
                response.setActiveFeatures(sub.getFeatures());
                response.setStatusDescription("Đang sử dụng gói " + sub.getName());
            } else {
                response.setStatusDescription("Đang sử dụng gói Premium");
                response.setActiveFeatures(new ArrayList<>());
            }
        } else {
            response.setPremium(false);
            response.setPremiumType("FREE");
            response.setPremiumExpiresAt(null);
            response.setDaysRemaining(0);
            response.setStatusDescription("Tài khoản miễn phí (Có quảng cáo)");
            response.setActiveFeatures(List.of("Nghe nhạc có quảng cáo", "Không tải được nhạc offline"));
        }
        
        return response;
    }

    @Override
    public PremiumStatusResponse subscribe(String userId, String packageId) {
        initializeDefaultPackages();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Subscription sub = subscriptionRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", packageId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = calculateExpiryDate(now, sub.getDuration());

        long priceVal = sub.getPrice() != null ? sub.getPrice().longValue() : 0;
        long userCoins = user.getCoins() != null ? user.getCoins() : 0L;

        if (userCoins >= priceVal && priceVal > 0) {
            user.setCoins(userCoins - priceVal);
            log.info("Deducted {} coins from user {} for subscription", priceVal, user.getEmail());
        } else {
            log.info("Simulated successful card/bank payment of {} VND for subscription", priceVal);
        }

        user.setPremiumType(sub.getName());
        user.setPremiumExpiresAt(expiresAt);
        userRepository.save(user);

        log.info("User {} successfully subscribed to package {}", user.getEmail(), sub.getName());
        return getPremiumStatus(userId);
    }

    @Override
    public PremiumStatusResponse cancelSubscription(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPremiumExpiresAt(LocalDateTime.now());
        user.setPremiumType(null);
        userRepository.save(user);

        log.info("User {} cancelled their premium subscription", user.getEmail());
        return getPremiumStatus(userId);
    }

    // --- MOMO PAYMENT DELEGATION ---

    @Override
    public String initiateMomoPayment(String userId, String packageId) throws Exception {
        initializeDefaultPackages();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Subscription sub = subscriptionRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", packageId));

        String orderId = "MOMO_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        long amount = sub.getPrice().longValue();
        String orderInfo = "Thanh toan premium goi: " + sub.getName();
        String extraData = "userId=" + userId + "&packageId=" + packageId;

        // Gọi MomoService để lấy link thanh toán
        String payUrl = momoService.createPaymentUrl(orderId, amount, orderInfo, extraData);

        // Lưu PaymentTransaction vào database
        PaymentTransaction tx = new PaymentTransaction();
        tx.setUserId(userId);
        tx.setPackageId(packageId);
        tx.setPackageName(sub.getName());
        tx.setAmount(sub.getPrice());
        tx.setOrderId(orderId);
        tx.setStatus("PENDING");
        tx.setCreatedAt(LocalDateTime.now());
        tx.setUpdatedAt(LocalDateTime.now());
        paymentTransactionRepository.save(tx);

        return payUrl;
    }

    @Override
    public void processMomoIPN(Map<String, String> ipnParams) throws Exception {
        log.info("Received MoMo IPN Callback: {}", ipnParams);

        // Xác thực chữ ký bằng MomoService
        boolean isSignatureValid = momoService.verifySignature(ipnParams);
        if (!isSignatureValid) {
            log.warn("MoMo signature verification failed inside IPN callback!");
        }

        String orderId = ipnParams.get("orderId");
        String resultCode = ipnParams.get("resultCode");

        PaymentTransaction tx = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "orderId", orderId));

        if ("PENDING".equals(tx.getStatus())) {
            tx.setUpdatedAt(LocalDateTime.now());
            if ("0".equals(resultCode)) {
                tx.setStatus("SUCCESS");
                paymentTransactionRepository.save(tx);

                // Kích hoạt Premium cho User
                User user = userRepository.findById(tx.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", tx.getUserId()));
                Subscription sub = subscriptionRepository.findById(tx.getPackageId())
                        .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", tx.getPackageId()));

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresAt = calculateExpiryDate(now, sub.getDuration());

                user.setPremiumType(sub.getName());
                user.setPremiumExpiresAt(expiresAt);
                userRepository.save(user);
                
                log.info("Successfully activated Premium {} for user {}", sub.getName(), user.getEmail());
            } else {
                tx.setStatus("FAILED");
                paymentTransactionRepository.save(tx);
                log.info("Payment failed for orderId: {}, resultCode: {}", orderId, resultCode);
            }
        } else {
            log.info("Transaction {} already processed with status: {}", orderId, tx.getStatus());
        }
    }

    // --- HELPER METHODS ---

    private LocalDateTime calculateExpiryDate(LocalDateTime start, String duration) {
        if ("P7D".equalsIgnoreCase(duration)) {
            return start.plusDays(7);
        } else if ("P30D".equalsIgnoreCase(duration)) {
            return start.plusDays(30);
        } else if (duration != null && duration.startsWith("P") && duration.endsWith("D")) {
            try {
                int days = Integer.parseInt(duration.substring(1, duration.length() - 1));
                return start.plusDays(days);
            } catch (Exception e) {
                return start.plusDays(30);
            }
        }
        return start.plusDays(30);
    }
}
