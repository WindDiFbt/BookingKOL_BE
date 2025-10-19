package com.web.bookingKol.domain.payment.dtos.transaction;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;


@Data
@Builder
public class TransactionReportDTO {
    private TransactionStatsDTO stats;

    private Page<TransactionResponseDTO> transactions;
}