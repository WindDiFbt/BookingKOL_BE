package com.web.bookingKol.common.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String code) {
        String subject = "Xác thực tài khoản - Nexussocial";
        String verifyUrl = "http://52.220.206.47/api/v1/register/verify?email=" + toEmail + "&code=" + code;
        String content = "Xin chào!\n\nVui lòng click link dưới đây để xác thực tài khoản của bạn:\n"
                + verifyUrl + "\n\nLink có hiệu lực trong 24h.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public void sendSimpleEmail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }

    public void sendPaymentReminderEmail(String toEmail, String userName, String contractNumber, int installmentNumber, BigDecimal amount, LocalDate dueDate) {
        String subject = "🔔 Nhắc đến hạn thanh toán hợp đồng #" + contractNumber;
        String content = String.format(
                "Xin chào %s,\n\n"
                        + "Đây là email nhắc nhở bạn về đợt thanh toán #%d của hợp đồng %s.\n"
                        + "Số tiền cần thanh toán: %,d VND\n"
                        + "Hạn thanh toán: %s\n\n"
                        + "Vui lòng truy cập hệ thống để thực hiện thanh toán đúng hạn.\n\n"
                        + "Trân trọng,\nĐội ngũ hỗ trợ Nexussocial",
                userName,
                installmentNumber,
                contractNumber,
                amount.intValue(),
                dueDate
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

}