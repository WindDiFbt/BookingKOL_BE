package com.web.bookingKol.domain.payment.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.payment.dtos.SePayWebhookRequest;
import com.web.bookingKol.domain.payment.dtos.TransactionResult;
import com.web.bookingKol.domain.payment.mappers.TransactionMapper;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Transaction;
import com.web.bookingKol.domain.payment.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SePayService {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TransactionRepository transactionRepository;

    private final String SEPAY_API_URL = "https://qr.sepay.vn/img?";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createQRCode(BigDecimal amount, String transferContent) {
        Merchant merchant = merchantService.getMerchantIsActive();
        String accountNumber = merchant.getVaNumber() != null ? merchant.getVaNumber() : merchant.getAccountNumber();
        String bank = merchant.getBank();
        return UriComponentsBuilder.fromUriString(SEPAY_API_URL)
                .queryParam("acc", accountNumber)
                .queryParam("bank", bank)
                .queryParam("amount", amount != null ? amount : "")
                .queryParam("des", transferContent)
                .toUriString();
    }

    @Transactional
    public ApiResponse<TransactionResult> handleWebhook(String receivedApiKey, SePayWebhookRequest request) {
        Merchant merchant = merchantService.getMerchantIsActive();
        if (receivedApiKey != null && receivedApiKey.startsWith("Apikey ")) {
            String key = receivedApiKey.substring(7);
            if (!passwordEncoder.matches(key, merchant.getApiKey())) {
                throw new IllegalArgumentException("Invalid API key");
            }
        } else {
            throw new IllegalArgumentException("Invalid API key");
        }
        try {
            LocalDateTime transactionDate = LocalDateTime.parse(
                    request.getTransactionDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            String content = request.getContent();
            Transaction tx = Transaction.builder()
                    .gateway(request.getGateway())
                    .transactionDate(transactionDate.atZone(ZoneOffset.UTC).toInstant())
                    .accountNumber(request.getAccountNumber())
                    .subAccount(request.getSubAccount())
                    .amountIn(request.getTransferType().equalsIgnoreCase("in") ? request.getTransferAmount() : BigDecimal.ZERO)
                    .amountOut(request.getTransferType().equalsIgnoreCase("out") ? request.getTransferAmount() : BigDecimal.ZERO)
                    .accumulated(request.getAccumulated())
                    .code(request.getCode())
                    .transactionContent(request.getContent())
                    .referenceNumber(request.getReferenceCode())
                    .createdAt(Instant.now())
                    .body(objectMapper.writeValueAsString(request))
                    .build();
            UUID contractId = null;
            if (content != null && !content.isEmpty()) {
                String fullUuid = content.trim().substring(0, 8) + "-" +
                        content.substring(8, 12) + "-" +
                        content.substring(12, 16) + "-" +
                        content.substring(16, 20) + "-" +
                        content.substring(20, 32);
                contractId = UUID.fromString(fullUuid);
            }
            if (contractId != null) {
                Optional<Contract> optionalContract = contractRepository.findById(contractId);
                if (optionalContract.isPresent()) {
                    Contract contract = optionalContract.get();
                    tx.setPayment(contract.getPayment());
                    transactionRepository.save(tx);
                    paymentService.updatePayment(transactionMapper.toDto(tx));
                } else {
                    transactionRepository.save(tx);
                }
            } else {
                transactionRepository.save(tx);
            }
            TransactionResult tr = TransactionResult.builder()
                    .contractId(contractId)
                    .status(Enums.TransactionStatus.COMPLETED.name())
                    .transactionId(tx.getId())
                    .build();
            return ApiResponse.<TransactionResult>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Transaction successfully!"))
                    .data(tr)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error saving webhook transaction: " + e.getMessage(), e);
        }
    }
}


