package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.booking.services.BookingRequestService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/admin/booking/single-requests")
public class AdminBookingRestController {
    @Autowired
    private BookingRequestService bookingRequestService;
    private static final String EXPORT_BOOKING_NAME_FILE = "Booking_";

    @GetMapping("/all")
    public ResponseEntity<?> getAllSingleRequest(@RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String requestNumber,
                                                 @RequestParam(required = false) LocalDate startAt,
                                                 @RequestParam(required = false) LocalDate endAt,
                                                 @RequestParam(required = false) LocalDate createdAtFrom,
                                                 @RequestParam(required = false) LocalDate createdAtTo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                null, null, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getDetailSingleRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(bookingRequestService.getDetailSingleRequestAdmin(requestId));
    }

    @GetMapping("/all/by-user/{userId}")
    public ResponseEntity<?> getAllSingleRequestByUser(@PathVariable("userId") UUID userId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String requestNumber,
                                                       @RequestParam(required = false) LocalDate startAt,
                                                       @RequestParam(required = false) LocalDate endAt,
                                                       @RequestParam(required = false) LocalDate createdAtFrom,
                                                       @RequestParam(required = false) LocalDate createdAtTo,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                null, userId, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/all/by-kol/{kolId}")
    public ResponseEntity<?> getAllSingleRequestByKol(@PathVariable("kolId") UUID kolId,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String requestNumber,
                                                      @RequestParam(required = false) LocalDate startAt,
                                                      @RequestParam(required = false) LocalDate endAt,
                                                      @RequestParam(required = false) LocalDate createdAtFrom,
                                                      @RequestParam(required = false) LocalDate createdAtTo,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                kolId, null, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/export/excel")
    public void exportBookingToExcel(HttpServletResponse response, @RequestParam(required = false) String type) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
        String fileName = EXPORT_BOOKING_NAME_FILE + currentDateTime + ".xlsx";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName;
        response.setHeader(headerKey, headerValue);
        ByteArrayInputStream inputStream = bookingRequestService.exportBookingDataToExcel(type);
        inputStream.transferTo(response.getOutputStream());
        inputStream.close();
        response.getOutputStream().flush();
    }

}
