package com.web.bookingKol.domain.user.services.impl;


import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.KolParticipantResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.BookingRequestParticipant;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestParticipantRepository;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.user.dtos.UserBookingRequestResponse;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.UserBookingRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBookingRequestServiceImpl implements UserBookingRequestService {

    private final UserRepository userRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final BookingRequestParticipantRepository bookingRequestParticipantRepository;

    @Override
    public ApiResponse<PagedResponse<UserBookingRequestResponse>> getUserBookingRequests(String userEmail, Pageable pageable) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        Page<BookingRequest> page = bookingRequestRepository.findByUser_IdAndCampaignIsNotNull(user.getId(), pageable);


        Page<UserBookingRequestResponse> mapped = page.map(br -> {
            Contract latestContract = br.getContracts().stream()
                    .max(Comparator.comparing(Contract::getCreatedAt))
                    .orElse(null);

            List<BookingRequestParticipant> participants =
                    bookingRequestParticipantRepository.findByBookingRequest_Id(br.getId());

            List<KolParticipantResponse> kolList = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.KOL)
                    .map(p -> KolParticipantResponse.builder()
                            .kolId(p.getKol().getId())
                            .kolName(p.getKol().getDisplayName())
                            .role(p.getRole().name())
                            .build())
                    .toList();

            List<KolParticipantResponse> liveList = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.LIVE)
                    .map(p -> KolParticipantResponse.builder()
                            .kolId(p.getKol().getId())
                            .kolName(p.getKol().getDisplayName())
                            .role(p.getRole().name())
                            .build())
                    .toList();


            return UserBookingRequestResponse.from(br, latestContract)
                    .toBuilder()
                    .kolParticipants(kolList)
                    .liveParticipants(liveList)
                    .build();
        });

        return ApiResponse.<PagedResponse<UserBookingRequestResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách booking request thành công"))
                .data(PagedResponse.fromPage(mapped))
                .build();
    }


    @Override
    public ApiResponse<UserBookingRequestResponse> getBookingRequestDetail(UUID id, String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userEmail));

        var booking = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking request: " + id));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem booking request này");
        }

        Contract latestContract = booking.getContracts().stream()
                .max(Comparator.comparing(Contract::getCreatedAt))
                .orElse(null);

        return ApiResponse.<UserBookingRequestResponse>builder()
                .status(HttpStatus.OK.value())
                .message(java.util.List.of("Lấy chi tiết booking request thành công"))
                .data(UserBookingRequestResponse.from(booking, latestContract))
                .build();
    }
}

