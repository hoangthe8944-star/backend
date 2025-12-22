package com.example.beatboxcompany.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Bỏ qua các đường dẫn không cần Token (Login/Register/Swagger)
        // Việc này giúp tối ưu hiệu năng, không cần parse token cho trang đăng nhập
        String path = request.getRequestURI();
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/verify") ||
                path.contains("swagger") ||
                path.contains("v3/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }
        // 2. Lấy Header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cắt chuỗi để lấy Token
        jwt = authHeader.substring(7);

        try {
            // 4. Lấy Email từ Token
            userEmail = jwtService.extractUsername(jwt);

            // 5. Nếu có Email và chưa được xác thực trong Context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Kiểm tra danh sách quyền trước khi khởi tạo
                var authorities = userDetails.getAuthorities();
                if (authorities == null || authorities.isEmpty()) {
                    System.err.println(
                            "===> Filter Debug: User có email " + userEmail + " NHƯNG KHÔNG CÓ QUYỀN (ROLES)!");
                }

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    // Tạo token xác thực
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            "N/A", // Thử để chuỗi tạm thay vì null nếu vẫn lỗi
                            authorities);

                    // Thêm thông tin chi tiết (IP, Session ID...) vào Authentication
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Lưu vào SecurityContext -> Request này đã được xác thực!
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("===> Filter Debug: LỖI XÁC THỰC: " + e.getMessage());
            // Nếu token lỗi, hết hạn hoặc không parse được, ta cứ cho qua filter
            // Spring Security ở phía sau sẽ chặn lại trả về 403 nếu API yêu cầu quyền.
            // Có thể log lỗi ở đây nếu muốn: e.printStackTrace();
        }

        // 6. Cho phép request đi tiếp
        filterChain.doFilter(request, response);
    }
}