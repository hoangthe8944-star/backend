package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.JwtResponse;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Request.LoginRequest;
import com.example.beatboxcompany.Request.RegisterRequest;
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
@CrossOrigin(origins = { "http://localhost:5173","https://hoangthe8944-star.github.io/boxonline/" }, allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ============================================================
    // 1. ĐĂNG KÝ (Gửi OTP)
    // ============================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email này đã được sử dụng!");
            }

            // Tạo mã OTP 6 con số
            String otp = String.format("%06d", new Random().nextInt(1000000));

            // Khởi tạo User mới
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(new ArrayList<>(Collections.singleton("ROLE_USER")));

            // Các trường phục vụ xác thực OTP
            user.setEnabled(false); // Chưa cho phép đăng nhập
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // Mã có hiệu lực 5 phút

            userRepository.save(user);

            // GỬI MAIL QUA GMAIL
            emailService.sendOtpEmail(user.getEmail(), otp);

            return ResponseEntity.ok("Mã xác thực đã được gửi vào Email của bạn.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống khi đăng ký: " + e.getMessage());
        }
    }

    // ============================================================
    // 2. XÁC THỰC OTP (Nếu đúng -> Kích hoạt tài khoản -> Trả về Token luôn)
    // ============================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thông tin đăng ký.");
        }

        // Kiểm tra mã OTP và thời gian hết hạn
        if (user.getOtp() != null &&
                user.getOtp().equals(otp) &&
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {

            user.setEnabled(true); // Kích hoạt tài khoản thành công
            user.setOtp(null); // Xóa mã OTP để bảo mật
            user.setOtpExpiry(null);
            userRepository.save(user);

            // Tự động tạo Token để người dùng vào App ngay không cần login lại
            String token = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(new JwtResponse(
                    token, user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), true));
        }

        return ResponseEntity.badRequest().body("Mã OTP không chính xác hoặc đã hết hạn!");
    }

    // ============================================================
    // 3. ĐĂNG NHẬP (Chặn nếu chưa xác thực OTP)
    // ============================================================
    // @PostMapping("/login")
    // public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    // User user = userRepository.findByEmail(request.getEmail())
    // .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));

    // // Kiểm tra Enabled
    // if (!user.isEnabled()) {
    // return ResponseEntity.status(HttpStatus.FORBIDDEN)
    // .body("Vui lòng xác thực mã OTP gửi qua Email trước khi đăng nhập!");
    // }

    // authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(request.getEmail(),
    // request.getPassword()));

    // String token = jwtService.generateToken(user.getEmail());
    // return ResponseEntity.ok(new JwtResponse(
    // token, user.getId(), user.getUsername(), user.getEmail(), user.getRoles(),
    // true));
    // }

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