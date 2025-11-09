package com.web.bookingKol.domain.booking.jobrunr;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class BookingRequestJob {
    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    @Autowired
    private ContractRepository contractRepository;
    private final Logger logger = Logger.getLogger("CLOSE_REQUEST_JOB");

    @Transactional
    public void closeDraftRequest(UUID bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId).orElse(null);
        if (bookingRequest == null) {
            logger.warning("Booking request not found: " + bookingRequestId);
            return;
        }
        bookingRequest.setStatus(Enums.BookingStatus.CANCELLED.name());
        bookingRequest.setUpdatedAt(Instant.now());
        bookingRequestRepository.save(bookingRequest);
        Contract contract = bookingRequest.getContracts().stream().findFirst().orElse(null);
        if (contract != null) {
            contract.setStatus(Enums.ContractStatus.CANCELLED.name());
            contractRepository.save(contract);
        }
        logger.info("[JOBRUNR] Closed Draft Request " + bookingRequest.getId());
    }

    @Transactional
    public void autoSetInProgressStatus(UUID bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId).orElse(null);
        if (bookingRequest == null) {
            logger.warning("Booking request not found: " + bookingRequestId);
            return;
        }
        bookingRequest.setStatus(Enums.BookingStatus.IN_PROGRESS.name());
        bookingRequest.setUpdatedAt(Instant.now());
        bookingRequestRepository.save(bookingRequest);
    }
}
