package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import org.springframework.stereotype.Service;

@Service
public interface ContractService {
    Contract createNewContract(BookingRequest bookingRequest);

    void confirmContract(Contract contract);

    void cancelContract(Contract contract);
}
