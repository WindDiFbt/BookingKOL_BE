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
    public ApiResponse<KolAvailabilityDTO> createKolSchedule(UUID userId, KolAvailabilityDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy KOL"));
        KolProfile kol = user.getKolProfile();
        if (kol == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Người dùng không phải KOL"))
                    .build();
        }

        KolAvailability existing = kolAvailabilityRepository.findByKolIdAndDateRange(
                kol.getId(), dto.getStartAt(), dto.getEndAt()
        ).stream().findFirst().orElse(null);

        if (existing != null &&
                existing.getStartAt().equals(dto.getStartAt()) &&
                existing.getEndAt().equals(dto.getEndAt())) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Khoảng thời gian này đã bị trùng với lịch khác"))
                    .build();
        }

        try {
            KolAvailability availability = new KolAvailability();
            availability.setId(UUID.randomUUID());
            availability.setKol(kol);
            availability.setStartAt(dto.getStartAt());
            availability.setEndAt(dto.getEndAt());
            availability.setCreatedAt(Instant.now());
            availability.setStatus("AVAILABLE");

            if (dto.getWorkTimes() != null && !dto.getWorkTimes().isEmpty()) {
                List<KolWorkTime> workTimes = dto.getWorkTimes().stream().map(wtDto -> {
                    KolWorkTime wt = new KolWorkTime();
                    wt.setId(UUID.randomUUID());
                    wt.setAvailability(availability);
                    wt.setStartAt(wtDto.getStartAt());
                    wt.setEndAt(wtDto.getEndAt());
                    wt.setNote(wtDto.getNote());
                    wt.setStatus(wtDto.getStatus());
                    return wt;
                }).toList();
                availability.setWorkTimes(workTimes);
            }

            kolAvailabilityRepository.save(availability);

            String kolEmail = user.getEmail();
            if (kolEmail != null && !kolEmail.isEmpty()) {
                String subject = "Lịch làm việc mới đã được tạo";
                String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <h2 style="color:#2E86C1;">Xin chào %s 👋</h2>
                        <p>Lịch làm việc của bạn đã được <strong style="color:green;">tạo thành công</strong> 🎉</p>
                    
                        <div style="border:1px solid #ccc; padding:15px; border-radius:8px; background-color:#f9f9f9; margin:10px 0;">
                            <p><strong>🗓️ Thời gian bắt đầu:</strong> %s</p>
                            <p><strong>⏰ Thời gian kết thúc:</strong> %s</p>
                            <p><strong>📝 Số khung thời gian con:</strong> %d</p>
                        </div>
                    
                        <p>💡 Bạn có thể đăng nhập lại hệ thống <a href="#####" style="color:#2E86C1; text-decoration:none;">BookingKOL</a> để xem hoặc cập nhật lịch làm việc của mình.</p>
                    
                        <p style="margin-top:20px;">Trân trọng,<br><strong>Đội ngũ BookingKOL</strong></p>
                    </body>
                    </html>
                    """.formatted(
                        user.getFullName() != null ? user.getFullName() : "KOL",
                        dto.getStartAt(),
                        dto.getEndAt(),
                        dto.getWorkTimes() != null ? dto.getWorkTimes().size() : 0
                );
                emailService.sendHtmlEmail(kolEmail, subject, content);
            }

            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.CREATED.value())
                    .message(List.of("Tạo lịch thành công"))
                    .data(new KolAvailabilityDTO(availability))
                    .build();

        } catch (Exception e) {
            logger.error("Lỗi khi tạo lịch hoặc gửi mail: {}", e.getMessage(), e);
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Tạo lịch thất bại: " + e.getMessage()))
                    .build();
        }
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
        Page<KolAvailability> availabilities =
                kolAvailabilityRepository.findByKolIdAndDateRangePaged(kolId, startDate, endDate, pageable);

        List<KolAvailabilityDTO> dtoList = availabilities.getContent()
                .stream()
                .map(KolAvailabilityDTO::new)
                .toList();

        return ApiResponse.<List<KolAvailabilityDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách lịch làm việc thành công"))
                .data(dtoList)
                .build();
    }


    @Override
    public ApiResponse<Page<TimeSlotDTO>> getKolFreeTimes(
            UUID kolId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    ) {
        List<KolAvailability> availabilities = kolAvailabilityRepository.findAvailabilities(kolId, startDate, endDate);

        List<KolWorkTime> workTimes = kolWorkTimeRepository.findBookedTimes(kolId, startDate, endDate);


        List<TimeSlotDTO> allFreeSlots = new ArrayList<>();

        for (KolAvailability a : availabilities) {
            Instant freeStart = a.getStartAt();
            Instant freeEnd = a.getEndAt();

            List<KolWorkTime> overlaps = workTimes.stream()
                    .filter(w -> w.getStartAt().isBefore(freeEnd) && w.getEndAt().isAfter(freeStart))
                    .sorted(Comparator.comparing(KolWorkTime::getStartAt))
                    .collect(Collectors.toList());

            if (overlaps.isEmpty()) {
                allFreeSlots.add(new TimeSlotDTO(freeStart, freeEnd));
                continue;
            }

            Instant cursor = freeStart;
            for (KolWorkTime w : overlaps) {
                if (w.getStartAt().isAfter(cursor)) {
                    allFreeSlots.add(new TimeSlotDTO(cursor, w.getStartAt()));
                }
                if (w.getEndAt().isAfter(cursor)) {
                    cursor = w.getEndAt();
                }
            }

            if (cursor.isBefore(freeEnd)) {
                allFreeSlots.add(new TimeSlotDTO(cursor, freeEnd));
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFreeSlots.size());
        Page<TimeSlotDTO> paged = new PageImpl<>(allFreeSlots.subList(start, end), pageable, allFreeSlots.size());

        return ApiResponse.<Page<TimeSlotDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch trống của KOL thành công"))
                .data(paged)
                .build();
    }



    @Override
    @Transactional
    public ApiResponse<KolWorkTimeDTO> updateKolWorkTimeByAdmin(UUID workTimeId, KolWorkTimeDTO dto) {
        KolWorkTime workTime = kolWorkTimeRepository.findById(workTimeId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khung thời gian làm việc"));

        if (dto.getStartAt() != null) workTime.setStartAt(dto.getStartAt());
        if (dto.getEndAt() != null) workTime.setEndAt(dto.getEndAt());
        if (dto.getNote() != null) workTime.setNote(dto.getNote());
        if (dto.getStatus() != null) workTime.setStatus(dto.getStatus());

        kolWorkTimeRepository.save(workTime);

        try {
            KolAvailability availability = workTime.getAvailability();
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





}