package com.example.beatboxcompany.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    // // Áp dụng chính sách CORS cho TẤT CẢ các endpoint trong ứng dụng
    // registry.addMapping("/**")
    // .allowedOrigins(
    // // Cho phép domain frontend khi deploy trên GitHub Pages
    // "https://hoangthe8944-star.github.io",

    // // Cho phép domain frontend khi phát triển ở local
    // "http://localhost:5173",

    // // Thêm các địa chỉ frontend khác nếu có (ví dụ: localhost:3000)
    // "http://localhost:3000"
    // )
    // // Cho phép các phương thức HTTP cần thiết
    // .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    // // Cho phép tất cả các header được gửi lên
    // .allowedHeaders("*")
    // // Cho phép frontend đọc các header được gửi về (quan trọng cho
    // Authentication)
    // .exposedHeaders("Authorization")
    // // Cho phép gửi thông tin credentials như cookies (nếu có dùng)
    // .allowCredentials(true);
    // }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Cho phép mọi origin (DEV ONLY)
                .allowedOriginPatterns("*")

                // Cho phép tất cả method
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

                // Cho phép mọi header (Authorization, Content-Type...)
                .allowedHeaders("*")

                // FE đọc được Authorization header
                .exposedHeaders("Authorization")

                // CẦN nếu dùng JWT / cookie
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình này dùng cho Swagger UI và file tĩnh, giữ nguyên là đúng
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/3.52.5/");

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}