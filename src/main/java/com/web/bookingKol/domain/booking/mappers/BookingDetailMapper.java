package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.domain.booking.dtos.BookingDetailDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.kol.mappers.KolDetailMapper;
import com.web.bookingKol.domain.user.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public BookingDetailDTO toDto(BookingRequest bookingRequest) {
        if (bookingRequest == null) {
            return null;
        }
        BookingDetailDTO dto = new BookingDetailDTO();
        dto.setId(bookingRequest.getId());
        dto.setUser(userMapper.toDto(bookingRequest.getUser()));
        dto.setKol(kolDetailMapper.toDto(bookingRequest.getKol()));
        dto.setBookingType(bookingRequest.getBookingType());
        dto.setStatus(bookingRequest.getStatus());
        dto.setDescription(bookingRequest.getDescription());
        dto.setLocation(bookingRequest.getLocation());
        dto.setStartAt(bookingRequest.getStartAt());
        dto.setEndAt(bookingRequest.getEndAt());
        dto.setCreatedAt(bookingRequest.getCreatedAt());
        dto.setUpdatedAt(bookingRequest.getUpdatedAt());
        if (bookingRequest.getAttachedFiles() != null) {
            dto.setAttachedFiles(fileUsageMapper.toDtoSet(bookingRequest.getAttachedFiles()));
        }
        if (bookingRequest.getContracts() != null) {
            dto.setContracts(contractMapper.toDtoSet(bookingRequest.getContracts()));
        }
        return dto;
    }
}
