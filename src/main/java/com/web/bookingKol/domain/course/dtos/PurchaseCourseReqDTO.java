package com.web.bookingKol.domain.course.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PurchaseCourseReqDTO {
    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Định dạng số điện thoại Việt Nam không hợp lệ.")
    private String phone;
    @Email(message = "Định dạng email không hợp lệ.")
    @NotBlank(message = "Email không được để trống.")
    private String email;
}
