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
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        // Tìm xem email này đã có trong DB chưa
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // CỨ CÓ TRONG DB LÀ TIN TƯỞNG, CHỈ CẬP NHẬT TRẠNG THÁI VERIFIED NẾU NÓ ĐANG
            // FALSE
            User existingUser = userOptional.get();
            if (!existingUser.isVerified()) {
                existingUser.setVerified(true);
                userRepository.save(existingUser);
            }
            System.out.println("Tài khoản đã tồn tại, cho phép đăng nhập ngay: " + email);
        } else {
            // NẾU CHƯA CÓ THÌ MỚI TẠO MỚI VÀ LƯU VÀO DB
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setRoles(Collections.singletonList("ROLE_USER"));
            newUser.setVerified(true); // Google verify hộ rồi, ko cần làm gì thêm
            userRepository.save(newUser);
            System.out.println("Tạo mới tài khoản qua Google: " + email);
        }

        return oidcUser;
    }
}