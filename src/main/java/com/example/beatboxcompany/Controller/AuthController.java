package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.JwtResponse;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Request.LoginRequest;
import com.example.beatboxcompany.Security.JwtService;
import com.example.beatboxcompany.Service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "https://hoangthe8944-star.github.io" }, allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // 1. ĐĂNG KÝ (Instant Auth - Vào luôn không cần check mail)
    // 1. ĐĂNG KÝ: Lưu DB và bắt xác thực
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false); // ✅ Bắt buộc xác thực

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), token); // Gửi mail qua Resend

        return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.");
    }

    // 2. ĐĂNG NHẬP: Chặn nếu chưa xác thực
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản chưa xác thực email!");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity
                .ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), true));
    }

    // ============================================================
    // 2. XÁC THỰC EMAIL CHÍNH
    // ============================================================
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token không hợp lệ.");

        User user = userOpt.get();
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Mã xác thực đã hết hạn.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Xác thực thành công! Giờ bạn có thể nghe nhạc.");
    }

    // ============================================================
    // 4. YÊU CẦU LIÊN KẾT EMAIL PHỤ (Phải đã đăng nhập)
    // ============================================================
    @PostMapping("/link-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestLink(@RequestParam String newEmail) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail).get();

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(30)); // 30p để liên kết
        userRepository.save(user);

        // Gửi mail đến email mới
        emailService.sendLinkEmail(newEmail, token);

        return ResponseEntity.ok("Yêu cầu đã gửi. Vui lòng kiểm tra hộp thư của " + newEmail);
    }

    // ============================================================
    // 5. XÁC NHẬN LIÊN KẾT EMAIL PHỤ
    // ============================================================
    @GetMapping("/link-confirm")
    public ResponseEntity<?> confirmLink(@RequestParam String token, @RequestParam String email) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty())
            return ResponseEntity.badRequest().body("Link liên kết không hợp lệ.");

        User user = userOpt.get();
        if (!user.getLinkedEmails().contains(email)) {
            user.getLinkedEmails().add(email);
        }

        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Liên kết email " + email + " thành công!");
    }

    // AuthController.java

    @PostMapping("/set-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setPassword(@RequestBody Map<String, String> request) {
        String newPassword = request.get("password");

        // 1. Lấy email người dùng hiện tại từ SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm user trong DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 3. Kiểm tra nếu đã có mật khẩu rồi (tùy chọn bảo mật)
        // if (user.getPassword() != null) return ResponseEntity.badRequest().body("Tài
        // khoản đã có mật khẩu");

        // 4. Mã hóa và lưu mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Thiết lập mật khẩu thành công! Bây giờ bạn có thể đăng nhập bằng email.");
    }
}