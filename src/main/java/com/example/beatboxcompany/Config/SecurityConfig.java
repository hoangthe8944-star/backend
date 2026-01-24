package com.example.beatboxcompany.Config;

import com.example.beatboxcompany.Security.CustomUserDetailsService;
import com.example.beatboxcompany.Security.JwtAuthenticationFilter;
import com.example.beatboxcompany.Security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
                                // 1. Dùng IF_REQUIRED để giữ session tạm cho Google OAuth2
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/api/public/**", "/api/songs/**",
                                                                "/api/v1/lyrics/**",
                                                                "/api/playlists/**", "/api/history/**",
                                                                "/api/categories/**", "/api/artists/**",
                                                                "/api/live/active")
                                                .permitAll()
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error",
                                                                "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/api/live/start", "/api/live/end/**").authenticated()
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())

                                // ✅ QUAN TRỌNG: Nếu API lỗi, trả về 401 chứ KHÔNG chuyển hướng sang Google
                                .exceptionHandling(e -> e
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(u -> u.oidcUserService(oauth2UserService))
                                                .successHandler((request, response, authentication) -> {
                                                        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

                                                        // ✅ PHẢI lấy Email, đừng lấy oidcUser.getName() vì nó có thể
                                                        // trả về Google ID
                                                        // (dãy số)
                                                        String email = oidcUser.getEmail();

                                                        System.out.println("===> JWT DEBUG: Đang tạo Token cho Email: "
                                                                        + email);

                                                        String token = jwtService.generateToken(email);

                                                        String targetUrl = "https://hoangthe8944-star.github.io/boxonline/#/login-success?token="
                                                                        + token;
                                                        response.sendRedirect(targetUrl);
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOriginPatterns(List.of(
                                "http://10.18.6.181",
                                "http://10.18.6.181:*",
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "https://boxonline-git-main-thes-projects-667db5e0.vercel.app/",
                                "https://hoangthe8944-star.github.io"));
                // ✅ DEV: cho phép mọi origin (không phụ thuộc IP / WiFi)
                // config.setAllowedOriginPatterns(List.of("*"));

                config.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                config.setAllowedHeaders(List.of("*"));

                // ✅ BẮT BUỘC vì bạn dùng JWT / OAuth2
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return source;
        }
}