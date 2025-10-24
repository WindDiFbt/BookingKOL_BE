package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.dtos.BookingSingleResDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.kol.mappers.KolWorkTimeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookingSingleResMapper {
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private KolWorkTimeMapper kolWorkTimeMapper;

    public BookingSingleResDTO toDto(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            return null;
        }
        BookingSingleResDTO dto = new BookingSingleResDTO();
        dto.setId(bookingRequest.getId());
        dto.setKolId(bookingRequest.getKol().getId());
        dto.setUserId(bookingRequest.getUser().getId());
        dto.setBookingType(bookingRequest.getBookingType());
        dto.setStatus(bookingRequest.getStatus());
        dto.setDescription(bookingRequest.getDescription());
        dto.setLocation(bookingRequest.getLocation());
        dto.setStartAt(bookingRequest.getStartAt());
        dto.setEndAt(bookingRequest.getEndAt());
        dto.setCreatedAt(bookingRequest.getCreatedAt());
        dto.setUpdatedAt(bookingRequest.getUpdatedAt());
        if (bookingRequest.getAttachedFiles() != null) {
            Set<FileUsage> activeAttachedFiles = bookingRequest.getAttachedFiles().stream()
                    .filter(attachedFile ->
                            attachedFile.getFile() != null &&
                                    !attachedFile.getFile().getStatus().equals(Enums.FileStatus.DELETED.name()))
                    .collect(Collectors.toSet());
            dto.setAttachedFiles(fileUsageMapper.toDtoSet(activeAttachedFiles));
        }
        if (bookingRequest.getContracts() != null) {
            dto.setContracts(contractMapper.toDtoSet(bookingRequest.getContracts()));
        }
        if (bookingRequest.getKolWorkTimes() != null) {
            dto.setKolWorkTimes(kolWorkTimeMapper.toDtoSet(bookingRequest.getKolWorkTimes()));
        }
        return dto;
    }
}
