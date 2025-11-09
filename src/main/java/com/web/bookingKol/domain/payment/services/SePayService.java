package com.web.bookingKol.domain.payment.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.payment.dtos.SePayWebhookRequest;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionResult;
import com.web.bookingKol.domain.payment.mappers.TransactionMapper;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Transaction;
import com.web.bookingKol.domain.payment.repositories.TransactionRepository;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private PurchasedCoursePackageRepository purchasedCoursePackageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger("TRANSACTION_LOGGER");
    @Autowired
    private ContractPaymentScheduleRepository contractPaymentScheduleRepository;

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
                    .message(List.of("Webhook đã nhận nhưng nội dung yêu cầu không thể đọc được."))
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
                    throw new IllegalArgumentException("Khóa API không hợp lệ");
                }
            } else {
                throw new IllegalArgumentException("Định dạng khóa API không hợp lệ");
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
                Optional<PurchasedCoursePackage> purchasedCoursePackageOptional = purchasedCoursePackageRepository.findById(contractId);
                Optional<ContractPaymentSchedule> contractPaymentScheduleOptional = contractPaymentScheduleRepository.findById(contractId);
                if (optionalContract.isPresent()) {
                    Contract contract = optionalContract.get();
                    tx.setPayment(contract.getPayment());
                    tx.setStatus(Enums.TransactionStatus.COMPLETED.name());
                    transactionRepository.save(tx);
                    paymentService.updatePaymentAfterTransactionSuccess(transactionMapper.toDto(tx));
                    sendEmailNotification(contract.getPayment().getUser(), contract);
                } else if (purchasedCoursePackageOptional.isPresent()) {
                    PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageOptional.get();
                    tx.setPayment(purchasedCoursePackageOptional.get().getPayment());
                    tx.setStatus(Enums.TransactionStatus.COMPLETED.name());
                    paymentService.updateCoursePaymentAfterTransactionSuccess(transactionMapper.toDto(tx));
                    sendEmailNotification(purchasedCoursePackage.getPayment().getUser(), purchasedCoursePackage);
                    transactionRepository.save(tx);
                } else if (contractPaymentScheduleOptional.isPresent()) {
                    ContractPaymentSchedule contractPaymentSchedule = contractPaymentScheduleOptional.get();
                    tx.setPaymentSchedule(contractPaymentSchedule);
                    tx.setStatus(Enums.TransactionStatus.COMPLETED.name());
                    paymentService.updatePaymentForCampaignAfterTransactionSuccess(contractPaymentSchedule.getId());
                    sendEmailNotification(contractPaymentSchedule.getContract().getBookingRequest().getUser(), contractPaymentSchedule);
                    transactionRepository.save(tx);
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
                    .message(List.of("Giao dịch thành công!"))
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
                    .message(List.of("Lỗi xử lý giao dịch: " + e.getMessage()))
                    .data(tr)
                    .build();
        }
    }

    private void sendEmailNotification(User user, Contract contract) {
        if (user == null || user.getEmail() == null) {
            logger.log(Level.WARNING, "Không thể gửi email: Thiếu thông tin người dùng hoặc email cho ID hợp đồng: " + contract.getId());
            return;
        }
        String subject = "🔔 Xác nhận Thanh toán Thành công (Hợp đồng " + contract.getContractNumber() + ")";
        String htmlContent = generatePaymentSuccessHtml(user, contract);
        try {
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            logger.log(Level.INFO, "Email xác nhận thanh toán thành công đã được gửi tới:" + user.getEmail());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi gửi email xác nhận thanh toán:" + e.getMessage());
        }
    }

    private String generatePaymentSuccessHtml(User user, Contract contract) {
        String formattedAmount = String.format("%,.0f VNĐ", contract.getAmount());
        String userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Xác nhận Thanh toán Thành công</title>
                    <style>
                        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .header { background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .details-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                        .details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #777; text-align: center; border-top: 1px solid #eee; padding-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Thanh Toán Thành Công! 🎉</h2>
                        </div>
                        <div class="content">
                            <p>Xin chào <strong>""" + userName + """
                </strong>,</p>
                <p>Chúng tôi xác nhận đã nhận được thanh toán của bạn cho hợp đồng/dịch vụ sau:</p>
                
                <table class="details-table">
                    <tr>
                        <th>Mã Hợp đồng</th>
                        <td>""" + contract.getContractNumber() + """
                    </td>
                </tr>
                <tr>
                    <th>Mã Yêu cầu</th>
                        <td>""" + contract.getBookingRequest().getRequestNumber() + """
                        </td>
                    </tr>
                <tr>
                    <th>Dịch vụ</th>
                    <td>""" + ("Dịch vụ booking KOL/KOC.") + """
                    </td>
                </tr>
                <tr>
                    <th>Số tiền đã thanh toán</th>
                    <td><strong>""" + formattedAmount + """
                    </strong></td>
                </tr>
                <tr>
                    <th>Thời gian thanh toán</th>
                    <td>""" + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")) + """
                                    </td>
                                </tr>
                            </table>
                
                            <p style="margin-top: 25px;">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi! Mọi thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.</p>
                        </div>
                        <div class="footer">
                            <p>Đây là email được gửi tự động. Vui lòng không trả lời email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private void sendEmailNotification(User user, PurchasedCoursePackage purchasedCoursePackage) {
        if (user == null || user.getEmail() == null) {
            logger.log(Level.WARNING, "Không thể gửi email: Thiếu thông tin người dùng hoặc email cho ID hợp đồng: " + purchasedCoursePackage.getId());
            return;
        }
        String subject = "🔔 Xác nhận Thanh toán Thành công (Khóa học livestream " + purchasedCoursePackage.getPurchasedCourseNumber() + ")";
        String htmlContent = generatePaymentSuccessHtml(user, purchasedCoursePackage);
        try {
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            logger.log(Level.INFO, "Email xác nhận thanh toán thành công đã được gửi tới:" + user.getEmail());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi gửi email xác nhận thanh toán:" + e.getMessage());
        }
    }

    private String generatePaymentSuccessHtml(User user, PurchasedCoursePackage purchasedCoursePackage) {
        String formattedAmount = String.format("%,d VNĐ", purchasedCoursePackage.getCurrentPrice());
        String userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Xác nhận Thanh toán Thành công</title>
                    <style>
                        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .header { background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .details-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                        .details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #777; text-align: center; border-top: 1px solid #eee; padding-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Thanh Toán Thành Công! 🎉</h2>
                        </div>
                        <div class="content">
                            <p>Xin chào <strong>""" + userName + """
                </strong>,</p>
                <p>Chúng tôi xác nhận đã nhận được thanh toán của bạn cho khóa học sau:</p>
                
                <table class="details-table">
                    <tr>
                        <th>Mã yêu cầu khóa học</th>
                        <td>""" + purchasedCoursePackage.getPurchasedCourseNumber() + """
                    </td>
                </tr>
                    <th>Dịch vụ</th>
                    <td>""" + ("Dịch vụ khóa học Livestream") + """
                    </td>
                </tr>
                <tr>
                    <th>Số tiền đã thanh toán</th>
                    <td><strong>""" + formattedAmount + """
                    </strong></td>
                </tr>
                <tr>
                    <th>Thời gian thanh toán</th>
                    <td>""" + Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")) + """
                                    </td>
                                </tr>
                            </table>
                
                            <p style="margin-top: 25px;">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi! Mọi thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.</p>
                        </div>
                        <div class="footer">
                            <p>Đây là email được gửi tự động. Vui lòng không trả lời email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private void sendEmailNotification(User user, ContractPaymentSchedule contractPaymentSchedule) {
        if (user == null || user.getEmail() == null) {
            logger.log(Level.WARNING, "Không thể gửi email: Thiếu thông tin người dùng hoặc email cho ID hợp đồng: " + contractPaymentSchedule.getId());
            return;
        }
        String subject = "🔔 Xác nhận Thanh toán Thành công đợt " + contractPaymentSchedule.getInstallmentNumber() +
                " (Hợp đồng " + contractPaymentSchedule.getContract().getContractNumber() + ")";
        String htmlContent = generatePaymentSuccessHtml(user, contractPaymentSchedule);
        try {
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            logger.log(Level.INFO, "Email xác nhận thanh toán thành công đã được gửi tới:" + user.getEmail());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi gửi email xác nhận thanh toán:" + e.getMessage());
        }
    }

    private String generatePaymentSuccessHtml(User user, ContractPaymentSchedule contractPaymentSchedule) {
        String formattedAmount = String.format("%,.0f VNĐ", contractPaymentSchedule.getAmount());
        String userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        String paymentTime = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        int installmentNumber = contractPaymentSchedule.getInstallmentNumber();
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Xác nhận Thanh Toán Đợt\s""" + installmentNumber + """
                Thành Công! Hợp đồng\s""" + contractPaymentSchedule.getContract().getContractNumber() + """
                     Thành công</title>
                    <style>
                        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .header { background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .details-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                        .details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #777; text-align: center; border-top: 1px solid #eee; padding-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                <div class="header">
                                        <h2>Thanh Toán Đợt\s""" + installmentNumber + """
                                  Thành Công!</h2>
                            </div>
                <div class="content">
                    <p>Xin chào <strong class="highlight">""" + userName + """
                </strong>,</p>
                <p>Chúng tôi xác nhận đã nhận được khoản thanh toán đợt <strong class="highlight">""" + installmentNumber + """
                </strong> cho hợp đồng của bạn.</p>
                
                <table class="details-table">
                    <tr>
                        <th>Mã Hợp đồng</th>
                        <td><span class="highlight">""" + contractPaymentSchedule.getContract().getContractNumber() + """
                    </span></td>
                </tr>
                <tr>
                    <th>Đợt Thanh toán</th>
                    <td>Đợt <strong>""" + installmentNumber + """
                    </strong></td>
                </tr>
                <tr>
                    <th>Số Tiền Đã Thanh Toán</th>
                    <td><strong>""" + formattedAmount + """
                    </strong></td>
                </tr>
                <tr>
                    <th>Dịch Vụ</th>
                    <td>Thanh toán đợt theo Hợp đồng</td>
                </tr>
                <tr>
                    <th>Thời Gian Thanh Toán</th>
                    <td>""" + paymentTime + """
                                    </td>
                                </tr>
                            </table>
                
                            <p style="margin-top: 25px;">Cảm ơn bạn đã thực hiện thanh toán đúng hạn! Đây là một phần quan trọng để tiếp tục sử dụng dịch vụ của chúng tôi.</p>
                            <p>Mọi thắc mắc liên quan đến hợp đồng, vui lòng liên hệ bộ phận hỗ trợ.</p>
                        </div>
                        <div class="footer">
                            <p>Đây là email được gửi tự động. Vui lòng không trả lời email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}


