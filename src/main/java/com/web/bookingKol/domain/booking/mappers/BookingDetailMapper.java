package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.domain.booking.dtos.BookingDetailDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.kol.mappers.KolDetailMapper;
import com.web.bookingKol.domain.kol.mappers.KolFeedbackMapper;
import com.web.bookingKol.domain.kol.mappers.KolWorkTimeMapper;
import com.web.bookingKol.domain.payment.mappers.RefundMapper;
import com.web.bookingKol.domain.user.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookingDetailMapper {
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private KolDetailMapper kolDetailMapper;
    @Autowired
    private KolWorkTimeMapper kolWorkTimeMapper;
    @Autowired
    private RefundMapper refundMapper;
    @Autowired
    private KolFeedbackMapper kolFeedbackMapper;

    public BookingDetailDTO toDto(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            return null;
        }
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setId(bookingRequest.getId());
        dto.setRequestNumber(bookingRequest.getRequestNumber());
        dto.setUser(userMapper.toDto(bookingRequest.getUser()));
        dto.setKol(kolDetailMapper.toDtoBasicInformation(bookingRequest.getKol()));
        dto.setBookingType(bookingRequest.getBookingType());
        dto.setStatus(bookingRequest.getStatus());
        dto.setDescription(bookingRequest.getDescription());
        dto.setLocation(bookingRequest.getLocation());
        dto.setStartAt(bookingRequest.getStartAt());
        dto.setEndAt(bookingRequest.getEndAt());
        dto.setCreatedAt(bookingRequest.getCreatedAt());
        dto.setUpdatedAt(bookingRequest.getUpdatedAt());
        dto.setFullName(bookingRequest.getFullName());
        dto.setPhone(bookingRequest.getPhone());
        dto.setEmail(bookingRequest.getEmail());
        dto.setPlatform(bookingRequest.getPlatform());
        if (bookingRequest.getAttachedFiles() != null) {
            dto.setAttachedFiles(
                    fileUsageMapper.toDtoSet(
                            bookingRequest.getAttachedFiles().stream()
                                    .filter(fu -> fu.getFile() != null && "ACTIVE".equalsIgnoreCase(fu.getFile().getStatus()))
                                    .collect(Collectors.toSet())
                    )
            );
        }
        Contract contract = bookingRequest.getContracts().stream().findFirst().orElse(null);
        if (contract != null) {
            dto.setContracts(contractMapper.toDtoSet(bookingRequest.getContracts()));
            if (contract.getRefund() != null) {
                dto.setRefundDTO(refundMapper.toDtoLighter(contract.getRefund()));
            }
            if (contract.getKolFeedbacks() != null) {
                dto.setFeedbackDTOS(contract.getKolFeedbacks().stream().map(kolFeedbackMapper::toDto).collect(Collectors.toSet()));
            }
        }
        if (bookingRequest.getKolWorkTimes() != null) {
            dto.setKolWorkTimes(kolWorkTimeMapper.toDtoSet(bookingRequest.getKolWorkTimes()));
        }
        return dto;
    }
}
