package com.example.beatboxcompany.Service;

import com.example.beatboxcompany.Dto.UserDto;
import com.example.beatboxcompany.Request.UserRegisterRequest;
import com.example.beatboxcompany.Entity.User;

import java.util.List;

public interface UserService {

    UserDto registerNewUser(UserRegisterRequest request);

    UserDto getUserById(String id);

    UserDto getUserByEmail(String email);

    List<UserDto> getAllUsers();

    void deleteUser(String userId);

    // ⭐ Thêm method cần thiết để lấy Entity User
    User findEntityByEmail(String email);

    String getUserIdByEmail(String email);
}
