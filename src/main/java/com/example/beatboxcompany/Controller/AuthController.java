package com.example.beatboxcompany.Controller;

import com.example.beatboxcompany.Dto.JwtResponse;
import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Request.LoginRequest;
import com.example.beatboxcompany.Request.UserRegisterRequest;
import com.example.beatboxcompany.Security.JwtService; // Đảm bảo tên file này đúng là JwtService hoặc JwtTokenProvider
import com.example.beatboxcompany.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService; // Nếu bạn đặt tên file là JwtTokenProvider thì sửa lại ở đây
    private final UserService userService;

    // ==================== LOGIN ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 1. Xác thực (Sẽ ném lỗi nếu sai email/pass)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. Set Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy User từ DB
            User user = userService.findEntityByEmail(request.getEmail());

            // 4. Tạo token
            String token = jwtService.generateToken(user.getEmail());

            // 5. Trả về kết quả
            // Lưu ý: user.getRoles() phải trả về List<String>. Nếu Entity của bạn là getRole() (số ít), hãy sửa lại Entity.
            JwtResponse response = new JwtResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles() 
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Đăng nhập thất bại: Sai email hoặc mật khẩu");
        }
    }

    // ==================== REGISTER ====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        try {
            // 1. Gọi Service để xử lý (Service đã lo việc check trùng mail, mã hóa pass, lưu DB)
            UserDto newUser = userService.registerNewUser(request);

            // 2. Tạo token ngay cho người dùng mới (để họ tự login luôn)
            String token = jwtService.generateToken(newUser.getEmail());

            // 3. Trả về Response
            JwtResponse response = new JwtResponse(
                    token,
                    newUser.getId(),
                    newUser.getUsername(),
                    newUser.getEmail(),
                    newUser.getRoles()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Bắt lỗi email đã tồn tại từ Service ném ra
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}