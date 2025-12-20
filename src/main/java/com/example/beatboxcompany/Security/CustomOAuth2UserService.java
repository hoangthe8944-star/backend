package com.example.beatboxcompany.Security;

import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        System.out.println("===> OAUTH2 DEBUG: Email nhận được từ Google: " + email);

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                System.out.println("===> OAUTH2 DEBUG: User chưa có. Đang tiến hành tạo mới...");
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setRoles(Collections.singletonList("ROLE_USER"));
                newUser.setVerified(true);

                // Ép in ra kết quả lưu
                User saved = userRepository.save(newUser);
                System.out.println("===> OAUTH2 DEBUG: LƯU THÀNH CÔNG! ID: " + saved.getId());
            } else {
                System.out.println("===> OAUTH2 DEBUG: User đã tồn tại, không lưu đè.");
            }
        } catch (Exception e) {
            System.err.println("===> OAUTH2 DEBUG: LỖI NGHIÊM TRỌNG KHI LƯU DB: " + e.getMessage());
            e.printStackTrace(); // In toàn bộ dấu vết lỗi ra Log của Render
        }

        return oAuth2User;
    }
}