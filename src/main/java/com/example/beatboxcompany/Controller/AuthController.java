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
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // 1. ĐĂNG KÝ (Gửi mail xác thực)
    // ============================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1. Nếu email đã có trong DB
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.ok("Email đã tồn tại. Bạn có thể đăng nhập ngay hoặc dùng Google.");
        }

        // 2. Nếu là người mới hoàn toàn -> Lưu vào DB và gửi mail xác thực cho chắc
        // chắn
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setVerified(false);
        user.setRoles(Collections.singletonList("ROLE_USER"));

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return ResponseEntity.ok("Đăng ký thành công! Hãy kiểm tra email một lần duy nhất để kích hoạt.");
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
    // 3. ĐĂNG NHẬP (Chặn nếu chưa verify)
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản chưa được xác thực email!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity
                .ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRoles()));
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

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // Lấy email từ JWT Token đã được Spring Security giải mã
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
}