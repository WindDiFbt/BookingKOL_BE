package com.web.bookingKol.domain.payment.dtos.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancellationRequest {
    @NotBlank(message = "Số tài khoản ngân hàng là bắt buộc")
    @Size(max = 20, message = "Số tài khoản ngân hàng tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]*$", message = "Số tài khoản ngân hàng chỉ được chứa các chữ số (0-9)")
    private String bankNumber;

    @NotBlank(message = "Tên ngân hàng là bắt buộc")
    @Size(max = 255, message = "Tên ngân hàng tối đa 255 ký tự")
    private String bankName;

    @NotBlank(message = "Tên chủ tài khoản là bắt buộc")
    @Size(max = 255, message = "Tên chủ tài khoản tối đa 255 ký tự")
    private String ownerName;

    @NotBlank(message = "Lý do là bắt buộc")
    @Size(max = 1000, message = "Lý do tối đa 1000 ký tự")
    private String reason;
}
