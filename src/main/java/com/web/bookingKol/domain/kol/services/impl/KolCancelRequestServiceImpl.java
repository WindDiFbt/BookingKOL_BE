package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.kol.dtos.KolCancelRequestDTO;
import com.web.bookingKol.domain.kol.models.KolCancelRequest;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolCancelRequestRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolCancelRequestService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KolCancelRequestServiceImpl implements KolCancelRequestService {

    private final KolCancelRequestRepository cancelRepo;
    private final KolWorkTimeRepository workTimeRepo;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ApiResponse<KolCancelRequestDTO> requestCancelWorkTimeByEmail(String email, UUID workTimeId, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với email: " + email));

        KolProfile kol = user.getKolProfile();
        if (kol == null) {
            return ApiResponse.<KolCancelRequestDTO>builder()
                    .status(400)
                    .message(List.of("Tài khoản này không phải là KOL"))
                    .build();
        }

        KolWorkTime workTime = workTimeRepo.findById(workTimeId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca làm"));

        if (!workTime.getAvailability().getKol().getId().equals(kol.getId())) {
            return ApiResponse.<KolCancelRequestDTO>builder()
                    .status(403)
                    .message(List.of("Ca làm này không thuộc về bạn"))
                    .build();
        }

        if (cancelRepo.existsByWorkTime_IdAndStatus(workTimeId, "PENDING")) {
            return ApiResponse.<KolCancelRequestDTO>builder()
                    .status(400)
                    .message(List.of("Bạn đã gửi yêu cầu hủy cho ca làm này rồi"))
                    .build();
        }

        KolCancelRequest req = new KolCancelRequest();
        req.setId(UUID.randomUUID());
        req.setKol(kol);
        req.setWorkTime(workTime);
        req.setReason(reason);
        req.setStatus("PENDING");
        req.setCreatedAt(Instant.now());

        cancelRepo.save(req);

        emailService.sendHtmlEmail(
                "admin@bookingkol.com",
                "Yêu cầu hủy ca làm mới từ KOL",
                "<p>KOL <b>" + user.getFullName() + "</b> (" + user.getEmail() + ")<br>" +
                        "đã gửi yêu cầu hủy ca làm:</p>" +
                        "<ul>" +
                        "<li>Bắt đầu: " + workTime.getStartAt() + "</li>" +
                        "<li>Kết thúc: " + workTime.getEndAt() + "</li>" +
                        "<li>Lý do: " + (reason != null ? reason : "(Không ghi rõ)") + "</li>" +
                        "</ul>"
        );

        return ApiResponse.<KolCancelRequestDTO>builder()
                .status(200)
                .message(List.of("Gửi yêu cầu hủy ca làm thành công"))
                .data(new KolCancelRequestDTO(req))
                .build();
    }


    @Transactional
    @Override
    public ApiResponse<String> approveCancelRequest(UUID requestId, String adminNote) {
        KolCancelRequest req = cancelRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu hủy"));

        KolWorkTime workTime = req.getWorkTime();

        if (workTime.getBookingRequest() != null) {

            emailService.sendHtmlEmail(
                    workTime.getBookingRequest().getUser().getEmail(),
                    "Thông báo: Ca làm của bạn bị hủy",
                    "<p>KOL đã hủy ca làm bạn đã đặt. Vui lòng chọn khung giờ khác.</p>"
            );
        }


        workTime.setStatus(Enums.KOLWorkTimeStatus.CANCELLED.name());


        workTimeRepo.save(workTime);

        req.setStatus("APPROVED");
        req.setAdminNote(adminNote);
        req.setApprovedAt(Instant.now());
        cancelRepo.save(req);


        emailService.sendHtmlEmail(req.getKol().getUser().getEmail(),
                "Yêu cầu hủy ca làm đã được phê duyệt",
                "<p>Yêu cầu hủy ca làm của bạn từ " + workTime.getStartAt() +
                        " đến " + workTime.getEndAt() + " đã được phê duyệt.</p>");

        emailService.sendHtmlEmail("admin@bookingkol.com",
                "Admin đã phê duyệt yêu cầu hủy ca làm",
                "<p>Ca làm từ " + workTime.getStartAt() + " đến " + workTime.getEndAt() + " đã được hủy.</p>");

        return ApiResponse.<String>builder()
                .status(200)
                .message(List.of("Phê duyệt hủy ca làm thành công"))
                .data("Yêu cầu hủy ID: " + requestId + " đã được phê duyệt")
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<String> rejectCancelRequest(UUID requestId, String adminNote) {
        KolCancelRequest req = cancelRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu hủy"));

        req.setStatus("REJECTED");
        req.setAdminNote(adminNote);
        req.setApprovedAt(Instant.now());
        cancelRepo.save(req);

        emailService.sendHtmlEmail(req.getKol().getUser().getEmail(),
                "Yêu cầu hủy ca làm bị từ chối",
                "<p>Yêu cầu hủy ca làm của bạn đã bị từ chối. Ghi chú: " + adminNote + "</p>");

        return ApiResponse.<String>builder()
                .status(200)
                .message(List.of("Từ chối yêu cầu hủy ca làm thành công"))
                .data("Yêu cầu hủy ID: " + requestId + " đã bị từ chối")
                .build();
    }
}

