package com.web.bookingKol.domain.booking.jobrunr;

import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ReminderEmailJob {
    private static final Logger logger = Logger.getLogger("REMINDER_EMAIL");
    @Autowired
    private EmailService emailService;
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private ContractRepository contractRepository;

    private static final DateTimeFormatter VIETNAM_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm 'ngày' dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public void sendWorkStartReminder(UUID workTimeId) {
        KolWorkTime workTime = kolWorkTimeRepository.findById(workTimeId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thời gian làm việc với ID: " + workTimeId));
        User user = workTime.getBookingRequest().getUser();
        Contract contract = contractRepository.findByWorkTimeId(workTimeId);
        if (contract == null) {
            throw new EntityNotFoundException("Không tìm thấy hợp đồng cho thời gian làm việc: " + workTime.getId());
        }
        String subject = "🔔 Nhắc nhở: Lịch làm việc sắp bắt đầu (Hợp đồng " + contract.getContractNumber() + ")";
        String htmlContent = generateWorkStartReminderHtml(user, contract);
        try {
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            logger.log(Level.INFO, "Đã gửi email nhắc nhở lịch làm việc đến: " + user.getEmail());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi khi gửi email nhắc nhở: " + e.getMessage());
        }
    }

    private String generateWorkStartReminderHtml(User user, Contract contract) {
        KolWorkTime kolWorkTime = contract.getBookingRequest().getKolWorkTimes().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thời gian làm việc cho hợp đồng: " + contract.getId()));
        String startTime = VIETNAM_FORMATTER.format(kolWorkTime.getStartAt());
        String endTime = VIETNAM_FORMATTER.format(kolWorkTime.getStartAt());
        String userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        String serviceDescription = "Dịch vụ booking KOL/KOC";
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Nhắc nhở Lịch làm việc</title>
                    <style>
                        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        /* Thay đổi màu header cho email thông báo/nhắc nhở */
                        .header { background-color: #007bff; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .details-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                        .details-table th, .details-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                        .footer { margin-top: 30px; font-size: 0.9em; color: #777; text-align: center; border-top: 1px solid #eee; padding-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Nhắc nhở: Lịch làm việc sắp diễn ra 🗓️</h2>
                        </div>
                        <div class="content">
                            <p>Xin chào <strong>""" + userName + """
                </strong>,</p>
                <p>Đây là thông báo nhắc nhở tự động. Dịch vụ bạn đã đặt sẽ bắt đầu sau khoảng 24 giờ nữa. Vui lòng kiểm tra thông tin chi tiết dưới đây:</p>
                
                <table class="details-table">
                    <tr>
                        <th>Mã Hợp đồng</th>
                        <td>""" + contract.getContractNumber() + """
                    </td>
                </tr>
                <tr>
                    <th>Dịch vụ</th>
                    <td>""" + serviceDescription + """
                    </td>
                </tr>
                <tr>
                    <th>Thời gian bắt đầu</th>
                    <td><strong>""" + startTime + """
                                    </strong></td>
                </tr>
                <tr>
                                   <th>Thời gian kết thúc</th>
                                   <td><strong>""" + endTime + """
                                                   </strong></td>
                               </tr>
                </table>
                            <p style="margin-top: 25px;">Vui lòng chuẩn bị sẵn sàng cho lịch làm việc. Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ bộ phận hỗ trợ.</p>
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
