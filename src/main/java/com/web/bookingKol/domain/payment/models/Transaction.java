package com.web.bookingKol.domain.payment.models;

import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "gateway", nullable = false, length = 100)
    private String gateway;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Size(max = 100)
    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Size(max = 250)
    @Column(name = "sub_account", length = 250)
    private String subAccount;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "amount_in", nullable = false, precision = 20, scale = 2)
    private BigDecimal amountIn;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "amount_out", nullable = false, precision = 20, scale = 2)
    private BigDecimal amountOut;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "accumulated", nullable = false, precision = 20, scale = 2)
    private BigDecimal accumulated;

    @Size(max = 250)
    @Column(name = "code", length = 250)
    private String code;

    @Column(name = "transaction_content", length = Integer.MAX_VALUE)
    private String transactionContent;

    @Size(max = 255)
    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "body", length = Integer.MAX_VALUE)
    private String body;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne()
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "status")
    private String status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_schedule_id")
    private ContractPaymentSchedule paymentSchedule;

}