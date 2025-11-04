package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingSingleReqDTO {
    @NotNull
    private UUID kolId;
    @NotBlank(message = "Họ và tên không được để trống.")
    @Size(max = 255, message = "Họ và tên phải ít hơn 255 ký tự.")
    private String fullName;
    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Định dạng số điện thoại Việt Nam không hợp lệ.")
    private String phone;
    @Email(message = "Định dạng email không hợp lệ.")
    @NotBlank(message = "Email không được để trống.")
    private String email;
    @NotNull
    private Instant startAt;
    @NotNull
    private Instant endAt;
    private String platform;
    private String description;
    private String location;
}
