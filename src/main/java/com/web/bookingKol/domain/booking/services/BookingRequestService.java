package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.BookingDetailDTO;
import com.web.bookingKol.domain.booking.dtos.BookingSingleReqDTO;
import com.web.bookingKol.domain.booking.dtos.BookingSingleResDTO;
import com.web.bookingKol.domain.booking.dtos.UpdateBookingReqDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public interface BookingRequestService {
    ApiResponse<PaymentReqDTO> createBookingSingleReq(UUID userId, BookingSingleReqDTO bookingRequestDTO, List<MultipartFile> attachedFiles);

    ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestAdmin(UUID kolId,
                                                                    UUID userId,
                                                                    String status,
                                                                    String requestNumber,
                                                                    LocalDate startAt,
                                                                    LocalDate endAt,
                                                                    LocalDate createdAtFrom,
                                                                    LocalDate createdAtTo,
                                                                    int page,
                                                                    int size);

    ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestUser(UUID userId,
                                                                   String status,
                                                                   String requestNumber,
                                                                   LocalDate startAt,
                                                                   LocalDate endAt,
                                                                   LocalDate createdAtFrom,
                                                                   LocalDate createdAtTo,
                                                                   int page,
                                                                   int size);

    ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestKol(UUID kolId,
                                                                  String status,
                                                                  String requestNumber,
                                                                  LocalDate startAt,
                                                                  LocalDate endAt,
                                                                  LocalDate createdAtFrom,
                                                                  LocalDate createdAtTo,
                                                                  int page,
                                                                  int size);

    ApiResponse<BookingDetailDTO> getDetailSingleRequestAdmin(UUID bookingRequestId);

    ApiResponse<BookingDetailDTO> getDetailSingleRequestKol(UUID bookingRequestId, UUID kolId);

    ApiResponse<BookingDetailDTO> getDetailSingleRequestUser(UUID bookingRequestId, UUID userId);

    ApiResponse<BookingDetailDTO> updateBookingRequest(UUID userId, UUID bookingRequestId, UpdateBookingReqDTO updateBookingReqDTO, List<MultipartFile> attachedFiles, List<UUID> fileIdsToDelete);

    ApiResponse<BookingDetailDTO> cancelBookingRequest(UUID userId, UUID bookingRequestId);

    void checkAndCompleteBookingRequest(BookingRequest bookingRequest);
}
