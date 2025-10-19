package com.web.bookingKol.domain.payment.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.payment.dtos.SePayWebhookRequest;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionResult;
import com.web.bookingKol.domain.payment.mappers.TransactionMapper;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Transaction;
import com.web.bookingKol.domain.payment.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
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
    private final Logger logger = Logger.getLogger("TRANSACTION_LOGGER");

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

//    public ApiResponse<TransactionResult> handleWebhook(String receivedApiKey, SePayWebhookRequest request) {
//        Merchant merchant = merchantService.getMerchantIsActive();
//        if (receivedApiKey != null && receivedApiKey.startsWith("Apikey ")) {
//            String key = receivedApiKey.substring(7);
//            if (!passwordEncoder.matches(key, merchant.getApiKey())) {
//                throw new IllegalArgumentException("Invalid API key");
//            }
//        } else {
//            throw new IllegalArgumentException("Invalid API key");
//        }
//        try {
//            LocalDateTime transactionDate = LocalDateTime.parse(
//                    request.getTransactionDate(),
//                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//            );
//            String content = request.getContent();
//            Transaction tx = Transaction.builder()
//                    .gateway(request.getGateway())
//                    .transactionDate(transactionDate.atZone(ZoneOffset.UTC).toInstant())
//                    .accountNumber(request.getAccountNumber())
//                    .subAccount(request.getSubAccount())
//                    .amountIn(request.getTransferType().equalsIgnoreCase("in") ? request.getTransferAmount() : BigDecimal.ZERO)
//                    .amountOut(request.getTransferType().equalsIgnoreCase("out") ? request.getTransferAmount() : BigDecimal.ZERO)
//                    .accumulated(request.getAccumulated())
//                    .code(request.getCode())
//                    .transactionContent(request.getContent())
//                    .referenceNumber(request.getReferenceCode())
//                    .createdAt(Instant.now())
//                    .body(objectMapper.writeValueAsString(request))
//                    .build();
//            UUID contractId = null;
//            if (content != null && !content.isEmpty()) {
//                String[] parts = content.trim().split("\\s+");
//                for (String part : parts) {
//                    if (part.length() == 32) {
//                        try {
//                            String fullUuid = part.substring(0, 8) + "-" +
//                                    part.substring(8, 12) + "-" +
//                                    part.substring(12, 16) + "-" +
//                                    part.substring(16, 20) + "-" +
//                                    part.substring(20, 32);
//                            contractId = UUID.fromString(fullUuid);
//                            break;
//                        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
//                            System.err.println("Đã tìm thấy phần tử 32 ký tự nhưng không phải UUID hợp lệ, bỏ qua: " + part);
//                        }
//                    }
//                }
//            }
//            if (contractId != null) {
//                Optional<Contract> optionalContract = contractRepository.findById(contractId);
//                if (optionalContract.isPresent()) {
//                    Contract contract = optionalContract.get();
//                    tx.setPayment(contract.getPayment());
//                    transactionRepository.save(tx);
//                    paymentService.updatePayment(transactionMapper.toDto(tx));
//                } else {
//                    transactionRepository.save(tx);
//                }
//            } else {
//                transactionRepository.save(tx);
//            }
//            TransactionResult tr = TransactionResult.builder()
//                    .contractId(contractId)
//                    .status(Enums.TransactionStatus.COMPLETED.name())
//                    .transactionId(tx.getId())
//                    .build();
//            return ApiResponse.<TransactionResult>builder()
//                    .status(HttpStatus.OK.value())
//                    .message(List.of("Transaction successfully!"))
//                    .data(tr)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException("Error saving webhook transaction: " + e.getMessage(), e);
//        }
//    }

    public ApiResponse<TransactionResult> handleWebhook(String receivedApiKey, SePayWebhookRequest request) {
        Transaction tx;
        String rawBody = "";
        TransactionResult tr = null;
        UUID contractId = null;
        try {
            rawBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return ApiResponse.<TransactionResult>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Webhook received but request body was unreadable."))
                    .build();
        }
        LocalDateTime transactionDate = null;
        try {
            transactionDate = LocalDateTime.parse(
                    request.getTransactionDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        tx = Transaction.builder()
                .gateway(request.getGateway())
                .transactionDate(transactionDate != null ? transactionDate.atZone(ZoneOffset.UTC).toInstant() : null)
                .accountNumber(request.getAccountNumber())
                .subAccount(request.getSubAccount())
                .amountIn(request.getTransferType().equalsIgnoreCase("in") ? request.getTransferAmount() : BigDecimal.ZERO)
                .amountOut(request.getTransferType().equalsIgnoreCase("out") ? request.getTransferAmount() : BigDecimal.ZERO)
                .accumulated(request.getAccumulated())
                .code(request.getCode())
                .transactionContent(request.getContent())
                .referenceNumber(request.getReferenceCode())
                .createdAt(Instant.now())
                .body(rawBody)
                .status(Enums.TransactionStatus.PENDING.name())
                .build();
        try {
            Merchant merchant = merchantService.getMerchantIsActive();
            if (receivedApiKey != null && receivedApiKey.startsWith("Apikey ")) {
                String key = receivedApiKey.substring(7);
                if (!passwordEncoder.matches(key, merchant.getApiKey())) {
                    throw new IllegalArgumentException("Invalid API key");
                }
            } else {
                throw new IllegalArgumentException("Invalid API key format");
            }
            String content = request.getContent();
            if (content != null && !content.isEmpty()) {
                String[] parts = content.trim().split("\\s+");
                for (String part : parts) {
                    if (part.length() == 32) {
                        try {
                            String fullUuid = part.substring(0, 8) + "-" +
                                    part.substring(8, 12) + "-" +
                                    part.substring(12, 16) + "-" +
                                    part.substring(16, 20) + "-" +
                                    part.substring(20, 32);
                            contractId = UUID.fromString(fullUuid);
                            break;
                        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                            logger.log(Level.WARNING, e.getMessage());
                        }
                    }
                }
            }
            if (contractId != null) {
                Optional<Contract> optionalContract = contractRepository.findById(contractId);
                if (optionalContract.isPresent()) {
                    Contract contract = optionalContract.get();
                    tx.setPayment(contract.getPayment());
                    tx.setStatus(Enums.TransactionStatus.COMPLETED.name());
                    transactionRepository.save(tx);
                    paymentService.updatePayment(transactionMapper.toDto(tx));
                } else {
                    transactionRepository.save(tx);
                }
            } else {
                tx.setStatus(Enums.TransactionStatus.ORPHANED.name());
                transactionRepository.save(tx);
            }
            tr = TransactionResult.builder()
                    .contractId(contractId)
                    .status(tx.getStatus())
                    .transactionId(tx.getId())
                    .build();
            return ApiResponse.<TransactionResult>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Transaction successfully!"))
                    .data(tr)
                    .build();

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            tx.setStatus(Enums.TransactionStatus.FAILED.name());
            transactionRepository.save(tx);
            tr = TransactionResult.builder()
                    .contractId(contractId)
                    .status(tx.getStatus())
                    .transactionId(tx.getId())
                    .build();
            return ApiResponse.<TransactionResult>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Error processing transaction: " + e.getMessage()))
                    .data(tr)
                    .build();
        }
    }
}


