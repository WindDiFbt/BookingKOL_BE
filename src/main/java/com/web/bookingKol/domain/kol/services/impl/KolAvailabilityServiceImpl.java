package com.web.bookingKol.domain.kol.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.dtos.TimeSlotDTO;
import com.web.bookingKol.domain.kol.dtos.WorkTimeDTO;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolAvailabilityService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KolAvailabilityServiceImpl implements KolAvailabilityService {

    private final KolAvailabilityRepository kolAvailabilityRepository;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(KolAvailabilityServiceImpl.class);
    @Autowired
    private EmailService emailService;
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private KolProfileRepository kolProfileRepository;

    @Override
    public ApiResponse<List<KolAvailabilityDTO>> getKolSchedule(UUID kolId, Instant start, Instant end) {
        var list = kolAvailabilityRepository.findByKolIdAndDateRange(kolId, start, end)
                .stream()
                .map(KolAvailabilityDTO::new)
                .toList();

        return ApiResponse.<List<KolAvailabilityDTO>>builder()
                .status(200)
                .message(List.of("Lấy thời khóa biểu thành công"))
                .data(list)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<KolAvailabilityDTO> createKolSchedule(UUID userId, KolAvailabilityDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        KolProfile kol = user.getKolProfile();
        if (kol == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Người dùng này không phải là KOL"))
                    .build();
        }

        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thiếu thời gian bắt đầu hoặc kết thúc"))
                    .build();
        }

        if (dto.getEndAt().isBefore(dto.getStartAt())) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thời gian kết thúc không thể trước thời gian bắt đầu"))
                    .build();
        }

        boolean overlapExists = kolAvailabilityRepository.findByKolIdAndDateRange(
                kol.getId(), dto.getStartAt(), dto.getEndAt()
        ).stream().anyMatch(existing ->
                !(existing.getEndAt().isBefore(dto.getStartAt()) || existing.getStartAt().isAfter(dto.getEndAt()))
        );

        if (overlapExists) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Khoảng thời gian này đã bị trùng với lịch làm việc khác"))
                    .build();
        }

        KolAvailability availability = new KolAvailability();
        availability.setId(UUID.randomUUID());
        availability.setKol(kol);
        availability.setStartAt(dto.getStartAt());
        availability.setEndAt(dto.getEndAt());
        availability.setCreatedAt(Instant.now());
        availability.setStatus("AVAILABLE");

        kolAvailabilityRepository.save(availability);

        try {
            String kolEmail = user.getEmail();
            if (kolEmail != null && !kolEmail.isEmpty()) {
                String subject = "🎉 Lịch làm việc mới đã được tạo thành công";
                String content = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color:#2E86C1;">Xin chào %s 👋</h2>
                    <p>Bạn vừa tạo thành công một lịch làm việc mới trên hệ thống BookingKOL 🎉</p>
                    
                    <div style="border:1px solid #ccc; padding:15px; border-radius:8px; background-color:#f9f9f9; margin:10px 0;">
                        <p><strong>🗓️ Thời gian bắt đầu:</strong> %s</p>
                        <p><strong>⏰ Thời gian kết thúc:</strong> %s</p>
                        <p><strong>🔖 Trạng thái:</strong> %s</p>
                    </div>
                    
                    <p>💡 Bạn có thể đăng nhập vào <a href="#####" style="color:#2E86C1; text-decoration:none;">BookingKOL</a> để xem và cập nhật lịch làm việc của mình.</p>
                    
                    <p style="margin-top:20px;">Trân trọng,<br><strong>Đội ngũ BookingKOL</strong></p>
                </body>
                </html>
                """.formatted(
                        user.getFullName() != null ? user.getFullName() : "KOL",
                        dto.getStartAt(),
                        dto.getEndAt(),
                        "AVAILABLE"
                );

                emailService.sendHtmlEmail(kolEmail, subject, content);
            }
        } catch (Exception e) {
            logger.warn("Tạo lịch thành công nhưng gửi mail thất bại: {}", e.getMessage());
        }

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message(List.of("Tạo lịch làm việc thành công"))
                .data(new KolAvailabilityDTO(availability))
                .build();
    }



    @Override
    public ApiResponse<KolAvailabilityDTO> getKolAvailabilityById(UUID availabilityId) {
        KolAvailability availability = kolAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch làm việc"));

        KolAvailabilityDTO dto = new KolAvailabilityDTO(availability);
        dto.setKolId(availability.getKol().getId());
        dto.setEmail(availability.getKol().getUser().getEmail());
        dto.setFullName(availability.getKol().getUser().getFullName());
        dto.setPhone(availability.getKol().getUser().getPhone());
        dto.setAvatarUrl(availability.getKol().getUser().getAvatarUrl());

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy thông tin lịch làm việc thành công"))
                .data(dto)
                .build();
    }


    @Override
    public ApiResponse<List<KolAvailabilityDTO>> getKolAvailabilitiesByKol(
            UUID kolId,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    ) {
        List<KolAvailability> availabilities =
                kolAvailabilityRepository.findAllWithWorkTimes(kolId, startDate, endDate);

        List<KolAvailabilityDTO> dtoList = availabilities.stream()
                .map(KolAvailabilityDTO::new)
                .toList();

        return ApiResponse.<List<KolAvailabilityDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách lịch làm việc cùng chi tiết thành công"))
                .data(dtoList)
                .build();
    }



    @Override
    public ApiResponse<List<TimeSlotDTO>> getKolFreeTimes(
            UUID kolId,
            Instant startDate,
            Instant endDate,
            Pageable pageable // có thể bỏ nếu không dùng nữa
    ) {

        List<KolAvailability> availabilities =
                kolAvailabilityRepository.findAvailabilities(kolId, startDate, endDate);


        List<KolWorkTime> workTimes =
                kolWorkTimeRepository.findAllActiveTimes(kolId, startDate, endDate);

        List<TimeSlotDTO> freeSlots = new ArrayList<>();


        for (KolAvailability availability : availabilities) {
            Instant freeStart = availability.getStartAt();
            Instant freeEnd = availability.getEndAt();


            List<KolWorkTime> overlaps = workTimes.stream()
                    .filter(w -> w.getStartAt().isBefore(freeEnd) && w.getEndAt().isAfter(freeStart))
                    .sorted(Comparator.comparing(KolWorkTime::getStartAt))
                    .collect(Collectors.toList());

            if (overlaps.isEmpty()) {
                freeSlots.add(new TimeSlotDTO(freeStart, freeEnd));
                continue;
            }

            Instant cursor = freeStart;
            for (KolWorkTime w : overlaps) {
                if (w.getStartAt().isAfter(cursor)) {
                    freeSlots.add(new TimeSlotDTO(cursor, w.getStartAt()));
                }
                if (w.getEndAt().isAfter(cursor)) {
                    cursor = w.getEndAt();
                }
            }

            if (cursor.isBefore(freeEnd)) {
                freeSlots.add(new TimeSlotDTO(cursor, freeEnd));
            }
        }

        return ApiResponse.<List<TimeSlotDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch trống của KOL thành công"))
                .data(freeSlots)
                .build();
    }





    @Override
    @Transactional
    public ApiResponse<KolWorkTimeDTO> updateKolWorkTimeByAdmin(UUID workTimeId, KolWorkTimeDTO dto) {
        KolWorkTime workTime = kolWorkTimeRepository.findById(workTimeId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khung thời gian làm việc"));

        KolAvailability availability = workTime.getAvailability();
        UUID kolId = availability.getKol().getId();

        Instant newStart = dto.getStartAt() != null ? dto.getStartAt() : workTime.getStartAt();
        Instant newEnd = dto.getEndAt() != null ? dto.getEndAt() : workTime.getEndAt();

        if (newEnd.isBefore(newStart)) {
            return ApiResponse.<KolWorkTimeDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thời gian kết thúc không thể trước thời gian bắt đầu"))
                    .build();
        }

        boolean isOverlapping = kolWorkTimeRepository.existsOverlappingBookingExceptSelf(
                kolId,
                workTimeId,
                newStart,
                newEnd
        );

        if (isOverlapping) {
            return ApiResponse.<KolWorkTimeDTO>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message(List.of("Khung giờ này bị trùng với lịch làm việc khác của KOL"))
                    .build();
        }

        workTime.setStartAt(newStart);
        workTime.setEndAt(newEnd);
        if (dto.getNote() != null) workTime.setNote(dto.getNote());
        if (dto.getStatus() != null) workTime.setStatus(dto.getStatus());
        workTime.setStatus("AVAILABLE");

        kolWorkTimeRepository.save(workTime);

        try {
            User kolUser = availability.getKol().getUser();
            String kolEmail = kolUser.getEmail();

            if (kolEmail != null && !kolEmail.isEmpty()) {
                String subject = "Cập nhật lịch làm việc của bạn";
                String content = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color:#E67E22;">Xin chào %s 👋</h2>
                    <p>Lịch làm việc của bạn đã được <strong style="color:blue;">cập nhật</strong> bởi quản trị viên.</p>
                    <div style="border:1px solid #ccc; padding:15px; border-radius:8px; background-color:#f9f9f9; margin:10px 0;">
                        <p><strong>🆔 ID khung thời gian:</strong> %s</p>
                        <p><strong>🗓️ Bắt đầu:</strong> %s</p>
                        <p><strong>⏰ Kết thúc:</strong> %s</p>
                        <p><strong>📋 Ghi chú:</strong> %s</p>
                        <p><strong>🔖 Trạng thái:</strong> %s</p>
                    </div>
                    <p>Vui lòng đăng nhập <a href="#####" style="color:#2E86C1; text-decoration:none;">BookingKOL</a> để xem lại lịch của bạn.</p>
                    <p style="margin-top:20px;">Trân trọng,<br><strong>Đội ngũ BookingKOL</strong></p>
                </body>
                </html>
            """.formatted(
                        kolUser.getFullName() != null ? kolUser.getFullName() : "KOL",
                        workTime.getId(),
                        workTime.getStartAt(),
                        workTime.getEndAt(),
                        workTime.getNote() != null ? workTime.getNote() : "(Không có)",
                        workTime.getStatus()
                );

                emailService.sendHtmlEmail(kolEmail, subject, content);
            }

            return ApiResponse.<KolWorkTimeDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Cập nhật lịch làm việc thành công"))
                    .data(new KolWorkTimeDTO(workTime))
                    .build();

        } catch (Exception e) {
            return ApiResponse.<KolWorkTimeDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Cập nhật thành công (nhưng gửi email thất bại)"))
                    .data(new KolWorkTimeDTO(workTime))
                    .build();
        }
    }





    // phần code admin thêm lịch cho kol
    @Override
    @Transactional
    public ApiResponse<KolAvailabilityDTO> createKolScheduleByAdmin(KolAvailabilityDTO dto) {

        if (dto.getKolId() == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thiếu ID của KOL"))
                    .build();
        }

        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thiếu thời gian bắt đầu hoặc kết thúc"))
                    .build();
        }

        if (dto.getEndAt().isBefore(dto.getStartAt())) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thời gian kết thúc không thể trước thời gian bắt đầu"))
                    .build();
        }

        KolProfile kol = kolProfileRepository.findById(dto.getKolId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy KOL với ID: " + dto.getKolId()));

        User user = kol.getUser();
        if (user == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("KOL này chưa gắn tài khoản người dùng"))
                    .build();
        }

        boolean isOverlapping = kolWorkTimeRepository.existsOverlappingBooking(
                dto.getKolId(),
                dto.getStartAt(),
                dto.getEndAt()
        );

        if (isOverlapping) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message(List.of("KOL đã có lịch trong khoảng thời gian này"))
                    .build();
        }

        KolAvailability availability = new KolAvailability();
        availability.setId(UUID.randomUUID());
        availability.setKol(kol);
        availability.setStartAt(dto.getStartAt());
        availability.setEndAt(dto.getEndAt());
        availability.setStatus("SUCCESS");
        availability.setCreatedAt(Instant.now());

        KolWorkTime workTime = new KolWorkTime();
        workTime.setId(UUID.randomUUID());
        workTime.setAvailability(availability);
        workTime.setStartAt(dto.getStartAt());
        workTime.setEndAt(dto.getEndAt());
        workTime.setStatus("AVAILABLE");
        workTime.setNote("Tự động tạo bởi ADMIN");

        availability.setWorkTimes(List.of(workTime));

        kolAvailabilityRepository.save(availability);


            String kolEmail = user.getEmail();
            if (kolEmail != null && !kolEmail.isEmpty()) {
                String subject = "Lịch làm việc mới được thêm bởi quản trị viên";
                String content = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color:#2E86C1;">Xin chào %s 👋</h2>
                    <p>Bạn vừa được <strong style="color:green;">quản trị viên</strong> thêm lịch làm việc mới 🎉</p>

                    <div style="border:1px solid #ccc; padding:15px; border-radius:8px; background-color:#f9f9f9; margin:10px 0;">
                        <p><strong>🗓️ Bắt đầu:</strong> %s</p>
                        <p><strong>⏰ Kết thúc:</strong> %s</p>
                    </div>

                    <p>💡 Bạn có thể đăng nhập hệ thống <a href="#####" style="color:#2E86C1; text-decoration:none;">BookingKOL</a> để xem chi tiết.</p>

                    <p style="margin-top:20px;">Trân trọng,<br><strong>Đội ngũ BookingKOL</strong></p>
                </body>
                </html>
                """.formatted(
                        user.getFullName() != null ? user.getFullName() : "KOL",
                        dto.getStartAt(),
                        dto.getEndAt()
                );

                emailService.sendHtmlEmail(kolEmail, subject, content);
            }

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message(List.of("Tạo lịch làm việc cho KOL thành công"))
                .data(new KolAvailabilityDTO(availability))
                .build();
    }




}