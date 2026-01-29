package com.example.beatboxcompany.Security;

import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // --- DEBUG LOG (Rất quan trọng để sửa lỗi 403) ---
        System.out.println("--- [DEBUG LOGIN] ---");
        System.out.println("Found User: " + user.getEmail());
        System.out.println("Roles from DB: " + user.getRoles());
        // ------------------------------------------------

        // 2. Map Roles từ String -> GrantedAuthority
        // Lưu ý: Trong DB phải là "ROLE_ADMIN", nếu chỉ là "ADMIN" thì dòng này phải tự
        // thêm "ROLE_"
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        // 3. Trả về UserDetails với đầy đủ cờ kích hoạt (Enabled = true)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                true, // enabled (Tài khoản đã kích hoạt)
                true, // accountNonExpired (Tài khoản chưa hết hạn)
                true, // credentialsNonExpired (Mật khẩu chưa hết hạn)
                true, // accountNonLocked (Tài khoản không bị khóa)
                user.getAuthorities());
    }
}