package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.kol.dtos.KolLeaveRequestDTO;
import com.web.bookingKol.domain.kol.models.*;
import com.web.bookingKol.domain.kol.repositories.*;
import com.web.bookingKol.domain.kol.services.KolLeaveRequestService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KolLeaveRequestServiceImpl implements KolLeaveRequestService {

    private final KolLeaveRequestRepository leaveRepo;
    private final KolAvailabilityRepository availabilityRepo;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final KolLeaveRequestRepository kolLeaveRequestRepository;

    @Override
    @Transactional
    public ApiResponse<KolLeaveRequestDTO> requestLeave(UUID kolId, UUID availabilityId, String reason) {
        KolAvailability availability = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch rảnh"));

        if (!availability.getKol().getId().equals(kolId)) {
            return ApiResponse.<KolLeaveRequestDTO>builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(List.of("Lịch này không thuộc về bạn"))
                    .build();
        }

        if (leaveRepo.existsByAvailability_IdAndStatus(availabilityId, "PENDING")) {
            return ApiResponse.<KolLeaveRequestDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Bạn đã gửi yêu cầu nghỉ cho lịch này rồi"))
                    .build();
        }

        KolLeaveRequest leave = new KolLeaveRequest();
        leave.setId(UUID.randomUUID());
        leave.setKol(availability.getKol());
        leave.setAvailability(availability);
        leave.setReason(reason);
        leave.setStatus("PENDING");
        leave.setCreatedAt(Instant.now());

        leaveRepo.save(leave);

        emailService.sendHtmlEmail("admin@bookingkol.com",
                "Yêu cầu nghỉ mới từ KOL " + availability.getKol().getUser().getFullName(),
                String.format("<p>KOL %s xin nghỉ từ %s đến %s</p>",
                        availability.getKol().getUser().getFullName(),
                        availability.getStartAt(),
                        availability.getEndAt())
        );

        return ApiResponse.<KolLeaveRequestDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Gửi yêu cầu nghỉ thành công"))
                .data(new KolLeaveRequestDTO(leave))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> approveLeave(UUID leaveRequestId, String adminNote) {
        KolLeaveRequest leave = leaveRepo.findById(leaveRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu nghỉ"));
        KolAvailability availability = leave.getAvailability();

        // Kiểm tra nếu ca làm có booking
        boolean hasBooking = availability.getWorkTimes().stream()
                .anyMatch(w -> w.getBookingRequest() != null);
        if (hasBooking) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message(List.of("Không thể duyệt vì có ca làm đã được đặt lịch"))
                    .build();
        }

        availabilityRepo.delete(availability);

        leave.setStatus("APPROVED");
        leave.setAdminNote(adminNote);
        leave.setApprovedAt(Instant.now());
        leaveRepo.save(leave);

        // Gửi email cho KOL
        User kolUser = leave.getKol().getUser();
        if (kolUser.getEmail() != null) {
            emailService.sendHtmlEmail(
                    kolUser.getEmail(),
                    "Yêu cầu nghỉ đã được phê duyệt ✅",
                    String.format("<p>Yêu cầu nghỉ của bạn từ %s đến %s đã được phê duyệt.</p>",
                            availability.getStartAt(),
                            availability.getEndAt())
            );
        }

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Phê duyệt yêu cầu nghỉ thành công, lịch rảnh đã bị xóa"))
                .data("Yêu cầu nghỉ ID " + leaveRequestId + " đã được phê duyệt.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> rejectLeave(UUID leaveRequestId, String adminNote) {
        KolLeaveRequest leave = leaveRepo.findById(leaveRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu nghỉ"));
        leave.setStatus("REJECTED");
        leave.setAdminNote(adminNote);
        leave.setApprovedAt(Instant.now());
        leaveRepo.save(leave);

        // Gửi mail từ chối
        User kolUser = leave.getKol().getUser();
        if (kolUser.getEmail() != null) {
            emailService.sendHtmlEmail(
                    kolUser.getEmail(),
                    "Yêu cầu nghỉ bị từ chối",
                    String.format("<p>Yêu cầu nghỉ của bạn từ %s đến %s đã bị từ chối.</p>",
                            leave.getAvailability().getStartAt(),
                            leave.getAvailability().getEndAt())
            );
        }

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Từ chối yêu cầu nghỉ thành công"))
                .data("Yêu cầu nghỉ ID " + leaveRequestId + " đã bị từ chối.")
                .build();
    }


    @Override
    public ApiResponse<Page<KolLeaveRequestDTO>> getMyLeaveRequests(UUID userId, int page, int size, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        KolProfile kol = user.getKolProfile();
        if (kol == null) {
            return ApiResponse.<Page<KolLeaveRequestDTO>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Người dùng này không phải là KOL"))
                    .build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KolLeaveRequest> pageResult;

        if (keyword != null && !keyword.trim().isEmpty()) {
            pageResult = kolLeaveRequestRepository.searchByKolAndReason(kol.getId(), keyword, pageable);
        } else {
            pageResult = kolLeaveRequestRepository.findByKol_Id(kol.getId(), pageable);
        }

        Page<KolLeaveRequestDTO> dtoPage = pageResult.map(KolLeaveRequestDTO::new);

        return ApiResponse.<Page<KolLeaveRequestDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách yêu cầu nghỉ thành công"))
                .data(dtoPage)
                .build();
    }


    @Override
    public ApiResponse<Page<KolLeaveRequestDTO>> getAllLeaveRequestsForAdmin(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KolLeaveRequest> pageResult;

        if ((keyword != null && !keyword.trim().isEmpty()) && (status != null && !status.trim().isEmpty())) {
            pageResult = kolLeaveRequestRepository.searchByKeywordAndStatus(keyword, status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            pageResult = kolLeaveRequestRepository.searchByKeyword(keyword, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            pageResult = kolLeaveRequestRepository.findByStatusIgnoreCase(status, pageable);
        } else {
            pageResult = kolLeaveRequestRepository.findAll(pageable);
        }

        Page<KolLeaveRequestDTO> dtoPage = pageResult.map(KolLeaveRequestDTO::new);

        return ApiResponse.<Page<KolLeaveRequestDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách yêu cầu nghỉ của tất cả KOL thành công"))
                .data(dtoPage)
                .build();
    }



}

