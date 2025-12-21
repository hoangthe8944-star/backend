package com.example.beatboxcompany.Config;

import com.example.beatboxcompany.Security.CustomUserDetailsService;
import com.example.beatboxcompany.Security.JwtAuthenticationFilter;
import com.example.beatboxcompany.Security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.beatboxcompany.Security.CustomOAuth2UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize ở Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final CustomOAuth2UserService oauth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép Login/Register
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. [QUAN TRỌNG] Cho phép API Public (Nghe nhạc, Xem danh sách Trending) không
                        // cần login
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/songs/**").permitAll()

                        // 3. [TÙY CHỌN] Cho phép Swagger UI (nếu dùng)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 4. [BẢO MẬT] Admin chỉ cho phép ROLE_ADMIN truy cập
                        // Dùng hasAuthority để khớp chính xác chuỗi "ROLE_ADMIN" trong MongoDB
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // 5. Các request còn lại (User/Artist) yêu cầu phải đăng nhập
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oauth2UserService))
                        .successHandler((request, response, authentication) -> {
                            // 1. Lấy email từ kết quả Google
                            String email = (String) ((org.springframework.security.oauth2.core.user.OAuth2User) authentication
                                    .getPrincipal()).getAttributes().get("email");

                            // 2. Tạo JWT Token
                            String token = jwtService.generateToken(email);

                            // 3. Redirect về Frontend (GitHub Pages) kèm Token
                            // Dùng HashRouter (#) để tránh lỗi 404 GitHub Pages
                            String targetUrl = "https://hoangthe8944-star.github.io/boxonline/#/login-success?token="
                                    + token;

                            response.sendRedirect(targetUrl);
                        }))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(e -> e
                        // Nếu chưa đăng nhập mà gọi API, chỉ trả về 401 Unauthorized, KHÔNG REDIRECT
                        .authenticationEntryPoint(
                                new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                        org.springframework.http.HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // =======================================================
        // === ĐỊNH NGHĨA CÁC NGUỒN ĐƯỢC PHÉP Ở ĐÂY ===
        // =======================================================
        config.setAllowedOrigins(List.of(
                "https://hoangthe8944-star.github.io", // Production Frontend
                "http://localhost:5173", // Development Frontend
                "http://localhost:3000" // Thêm các port dev khác nếu cần
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình này cho tất cả các đường dẫn
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}