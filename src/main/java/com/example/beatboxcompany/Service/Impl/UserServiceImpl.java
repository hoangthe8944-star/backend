package com.example.beatboxcompany.Service.Impl;

import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Mapper.UserMapper;
import com.example.beatboxcompany.Repository.UserRepository;
import com.example.beatboxcompany.Request.UserRegisterRequest;
import com.example.beatboxcompany.Service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto registerNewUser(UserRegisterRequest request) {
        // 1. Kiểm tra Email trùng lặp
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // 2. Map từ Request sang Entity
        User user = UserMapper.toEntity(request);

        // 3. MÃ HÓA MẬT KHẨU (Bước quan trọng nhất để Login được)
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Set Role mặc định (Nếu Mapper chưa làm hoặc muốn chắc chắn)
        // LƯU Ý: Sửa getRole() -> getRoles() và setRole() -> setRoles()
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        // 5. Lưu xuống DB
        User savedUser = userRepository.save(user);

        // 6. Trả về DTO
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có email: " + email));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Không tìm thấy người dùng để xóa.");
        }
        userRepository.deleteById(userId);
    }

    // Hàm này dùng cho AuthController (Login)
    @Override
    public User findEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // Thêm hàm này vào Interface và Impl
    public String getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}