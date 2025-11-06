package com.web.bookingKol.domain.kol.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.common.services.EmailService;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.dtos.TimeRangeDTO;
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

import java.time.Duration;
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

        Instant now = Instant.now();
        Instant minAllowedDate = now.plusSeconds(14L * 24 * 60 * 60);

        if (dto.getStartAt().isBefore(minAllowedDate)) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Bạn chỉ có thể đăng ký lịch rảnh trước ít nhất 14 ngày so với ngày hiện tại"))
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
    public ApiResponse<List<TimeSlotDTO>> getKolFreeTimes(UUID kolId, Instant startDate, Instant endDate, Pageable pageable) {

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

            Instant cursor = freeStart;

            if (overlaps.isEmpty()) {
                if (Duration.between(freeStart, freeEnd).toHours() >= 2) {
                    freeSlots.add(new TimeSlotDTO(freeStart, freeEnd));
                }
                continue;
            }

            for (KolWorkTime w : overlaps) {
                Instant endOfFree = w.getStartAt();
                long hoursFree = Duration.between(cursor, endOfFree).toHours();

                if (hoursFree >= 2) {
                    Instant adjustedStart = cursor.isBefore(freeStart) ? freeStart : cursor;
                    if (adjustedStart.isBefore(endOfFree)) {
                        freeSlots.add(new TimeSlotDTO(adjustedStart, endOfFree));
                    }
                }

                cursor = w.getEndAt().plus(Duration.ofHours(1));
            }

            if (cursor.isBefore(freeEnd)) {
                long hoursRemain = Duration.between(cursor, freeEnd).toHours();
                if (hoursRemain >= 2) {
                    freeSlots.add(new TimeSlotDTO(cursor, freeEnd));
                }
            }
        }

        return ApiResponse.<List<TimeSlotDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch rảnh hợp lệ của KOL thành công"))
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

        if (dto.getAvailabilityId() == null) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thiếu ID của lịch rảnh (availabilityId)"))
                    .build();
        }

        // Tìm availability sẵn có
        KolAvailability availability = kolAvailabilityRepository.findById(dto.getAvailabilityId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch rảnh với ID: " + dto.getAvailabilityId()));

        KolProfile kol = availability.getKol();
        User user = kol.getUser();

        // Validate thời gian hợp lệ
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

        // Kiểm tra khoảng này có nằm trong availability không
        if (dto.getStartAt().isBefore(availability.getStartAt()) || dto.getEndAt().isAfter(availability.getEndAt())) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Thời gian làm việc phải nằm trong khung rảnh của KOL"))
                    .build();
        }

        // Kiểm tra có bị trùng ca khác không
        boolean overlap = kolWorkTimeRepository.existsOverlappingBooking(
                kol.getId(),
                dto.getStartAt(),
                dto.getEndAt()
        );

        if (overlap) {
            return ApiResponse.<KolAvailabilityDTO>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message(List.of("KOL đã có ca làm việc trong khoảng thời gian này"))
                    .build();
        }

        KolWorkTime workTime = new KolWorkTime();
        workTime.setId(UUID.randomUUID());
        workTime.setAvailability(availability);
        workTime.setStartAt(dto.getStartAt());
        workTime.setEndAt(dto.getEndAt());
        workTime.setStatus("AVAILABLE");
        workTime.setNote(dto.getNote() != null ? dto.getNote() : "Tạo bởi ADMIN");

        kolWorkTimeRepository.save(workTime);

        // Gửi email
        try {
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
        } catch (Exception e) {
            // Không cản trở logic nếu email fail
        }

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message(List.of("Thêm ca làm việc vào lịch rảnh thành công"))
                .data(new KolAvailabilityDTO(availability))
                .build();
    }


    // admin xóa lịch rảnh cho kol
    @Override
    @Transactional
    public ApiResponse<String> deleteKolAvailabilityByAdmin(UUID availabilityId) {
        KolAvailability availability = kolAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch rảnh với ID: " + availabilityId));

        boolean hasBookedSlot = availability.getWorkTimes().stream()
                .anyMatch(wt -> wt.getBookingRequest() != null);

        if (hasBookedSlot) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Không thể xóa lịch rảnh này vì có ca làm đã được đặt lịch"))
                    .build();
        }

        User kolUser = availability.getKol().getUser();
        String kolEmail = kolUser != null ? kolUser.getEmail() : null;
        Instant startAt = availability.getStartAt();
        Instant endAt = availability.getEndAt();

        kolAvailabilityRepository.delete(availability);

        try {
            if (kolEmail != null && !kolEmail.isEmpty()) {
                String subject = "Lịch rảnh của bạn đã bị xóa bởi quản trị viên";
                String content = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2 style="color:#E74C3C;">Xin chào %s 👋</h2>
                <p>Lịch rảnh của bạn trong khoảng thời gian sau đã được <strong>quản trị viên xóa</strong> khỏi hệ thống:</p>
                <div style="border:1px solid #ccc; padding:15px; border-radius:8px; background-color:#f9f9f9; margin:10px 0;">
                    <p><strong>🗓️ Bắt đầu:</strong> %s</p>
                    <p><strong>⏰ Kết thúc:</strong> %s</p>
                </div>
                <p>💡 Nếu bạn có thắc mắc, vui lòng liên hệ lại bộ phận quản trị để được hỗ trợ.</p>
                <p style="margin-top:20px;">Trân trọng,<br><strong>Đội ngũ BookingKOL</strong></p>
            </body>
            </html>
            """.formatted(
                        kolUser.getFullName() != null ? kolUser.getFullName() : "KOL",
                        startAt,
                        endAt
                );

                emailService.sendHtmlEmail(kolEmail, subject, content);
            }
        } catch (Exception e) {
            logger.warn("Đã xóa lịch rảnh nhưng gửi email thất bại: {}", e.getMessage());
        }

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa lịch rảnh thành công"))
                .data("Lịch rảnh ID " + availabilityId + " đã được xóa thành công.")
                .build();
    }



    @Transactional
    @Override
    public ApiResponse<String> removeAvailabilityRange(String email, UUID availabilityId, TimeRangeDTO range) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        KolProfile kol = user.getKolProfile();
        if (kol == null)
            return ApiResponse.<String>builder()
                    .status(400)
                    .message(List.of("Tài khoản này không phải là KOL"))
                    .build();

        KolAvailability availability = kolAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch rảnh"));

        if (!availability.getKol().getId().equals(kol.getId()))
            return ApiResponse.<String>builder()
                    .status(403)
                    .message(List.of("Lịch này không thuộc về bạn"))
                    .build();

        Instant start = availability.getStartAt();
        Instant end = availability.getEndAt();

        if (!range.getStartRemove().isAfter(start) && !range.getEndRemove().isBefore(end)) {
            kolAvailabilityRepository.delete(availability);
            return ApiResponse.<String>builder()
                    .status(200)
                    .message(List.of("Đã xóa toàn bộ lịch rảnh"))
                    .build();
        }

        if (range.getStartRemove().equals(start)) {
            availability.setStartAt(range.getEndRemove());
            kolAvailabilityRepository.save(availability);
            return ApiResponse.<String>builder()
                    .status(200)
                    .message(List.of("Đã cắt bỏ phần đầu lịch rảnh"))
                    .build();
        }

        if (range.getEndRemove().equals(end)) {
            availability.setEndAt(range.getStartRemove());
            kolAvailabilityRepository.save(availability);
            return ApiResponse.<String>builder()
                    .status(200)
                    .message(List.of("Đã cắt bỏ phần cuối lịch rảnh"))
                    .build();
        }

        KolAvailability newBlock = new KolAvailability();
        newBlock.setId(UUID.randomUUID());
        newBlock.setKol(kol);
        newBlock.setStartAt(range.getEndRemove());
        newBlock.setEndAt(end);
        newBlock.setCreatedAt(Instant.now());
        newBlock.setStatus("AVAILABLE");

        availability.setEndAt(range.getStartRemove());

        kolAvailabilityRepository.save(availability);
        kolAvailabilityRepository.save(newBlock);

        return ApiResponse.<String>builder()
                .status(200)
                .message(List.of("Xóa lịch thành công"))
                .build();
    }





}