package com.web.bookingKol.domain.user.dtos;

import com.web.bookingKol.common.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAccountDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
    @Pattern(regexp = "^(0[3|5|7|8|9])([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    @Size(min = 10, max = 10, message = "Số điện thoại phải có 10 chữ số")
    private String phone;
    @NotBlank(message = "Mật khẩu không được để trống")
    @StrongPassword(message = "Mật khẩu không đủ mạnh")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    private String gender;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
}
