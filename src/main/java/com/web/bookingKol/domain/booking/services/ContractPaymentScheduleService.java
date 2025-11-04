package com.web.bookingKol.domain.booking.services;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.CreatePaymentScheduleRequest;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;

import java.util.List;

public interface ContractPaymentScheduleService {
    ApiResponse<?> createPaymentSchedule(CreatePaymentScheduleRequest request);



}

