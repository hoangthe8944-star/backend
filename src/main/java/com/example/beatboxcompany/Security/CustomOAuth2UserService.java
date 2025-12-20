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

        System.out.println(">>> Đang xử lý OAuth2 cho email: " + email);

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                System.out.println(">>> User mới! Đang tiến hành lưu vào MongoDB...");
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setRoles(Collections.singletonList("ROLE_USER"));
                newUser.setVerified(true);

                User savedUser = userRepository.save(newUser);
                System.out.println(">>> Đã lưu thành công User ID: " + savedUser.getId());
            } else {
                System.out.println(">>> User đã tồn tại trong DB, không cần lưu mới.");
            }
        } catch (Exception e) {
            System.err.println(">>> LỖI KHI LƯU USER VÀO DB: " + e.getMessage());
            e.printStackTrace();
        }

        return oAuth2User;
    }
}