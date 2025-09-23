package com.web.bookingKol.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private List<String> message;
    private T data;
    @Builder.Default
    private String timestamp = java.time.OffsetDateTime.now().toString();
}
