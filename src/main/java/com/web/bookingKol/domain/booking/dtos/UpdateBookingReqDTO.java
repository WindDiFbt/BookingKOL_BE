package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBookingReqDTO {
    @Size(max = 255, message = "Họ và tên phải ít hơn 255 ký tự.")
    private String fullName;
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Định dạng số điện thoại Việt Nam không hợp lệ.")
    private String phone;
    @NotBlank(message = "Email không được để trống.")
    private String email;
    private String description;
    private String location;
    private String platform;
}
