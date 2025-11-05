package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.domain.payment.models.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Service
public class QRGenerateService {
    @Autowired
    private MerchantService merchantService;
    private final String SEPAY_API_URL = "https://qr.sepay.vn/img?";

    public String createQRCode(BigDecimal amount, String transferContent) {
        Merchant merchant = merchantService.getMerchantIsActive();
        String accountNumber = merchant.getVaNumber() != null ? merchant.getVaNumber() : merchant.getAccountNumber();
        String bank = merchant.getBank();
        return UriComponentsBuilder.fromUriString(SEPAY_API_URL)
                .queryParam("acc", accountNumber)
                .queryParam("bank", bank)
                .queryParam("amount", amount != null ? amount : "")
                .queryParam("des", transferContent)
                .toUriString();
    }
}
