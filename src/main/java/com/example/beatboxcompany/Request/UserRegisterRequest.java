package com.example.beatboxcompany.Request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    // Trong Mapper bạn gọi getRole() -> Nên ở đây đặt là 'role'
    // Nếu bạn muốn đổi thành 'roles' thì phải sửa lại cả trong Mapper
    private List<String> role; 
    
    private String avatarUrl;
}