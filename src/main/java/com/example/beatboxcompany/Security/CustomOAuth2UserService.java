package com.example.beatboxcompany.Security;

import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService { // Kế thừa OidcUserService

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest); // Gọi hàm gốc để lấy dữ liệu từ Google
        
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        System.out.println("===> OIDC DEBUG: Đang xử lý đăng nhập cho email: " + email);

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                System.out.println("===> OIDC DEBUG: User mới! Đang tạo trong MongoDB...");
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setRoles(Collections.singletonList("ROLE_USER"));
                newUser.setVerified(true);
                
                userRepository.save(newUser);
                System.out.println("===> OIDC DEBUG: LƯU DB THÀNH CÔNG!");
            } else {
                System.out.println("===> OIDC DEBUG: User đã tồn tại, không lưu đè.");
            }
        } catch (Exception e) {
            System.err.println("===> OIDC DEBUG: LỖI KHI LƯU DB: " + e.getMessage());
            e.printStackTrace();
        }

        return oidcUser;
    }
}