package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.UpdateEntityUtil;
import com.web.bookingKol.domain.payment.dtos.MerchantDTO;
import com.web.bookingKol.domain.payment.dtos.MerchantRequest;
import com.web.bookingKol.domain.payment.mappers.MerchantMapper;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.repositories.MerchantRepository;
import com.web.bookingKol.domain.payment.services.MerchantService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<MerchantDTO> getAllMerchant(Pageable pageable) {
        Page<Merchant> merchantPage = merchantRepository.findAll(pageable);
        return merchantPage.map(merchantMapper::toDto);
    }

    @Override
    public MerchantDTO getDetailMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id: " + merchantId));
        return merchantMapper.toDto(merchant);
    }

    @Transactional
    @Override
    public MerchantDTO createMerchant(MerchantRequest merchantRequest) {
        deactivateAllMerchants();
        Merchant newMerchant = new Merchant();
        newMerchant.setId(UUID.randomUUID());
        newMerchant.setName(merchantRequest.getName());
        newMerchant.setApiKey(passwordEncoder.encode(merchantRequest.getApiKey()));
        newMerchant.setCreatedAt(Instant.now());
        newMerchant.setUpdatedAt(null);
        newMerchant.setBank(merchantRequest.getBank());
        newMerchant.setAccountNumber(merchantRequest.getAccountNumber());
        newMerchant.setVaNumber(merchantRequest.getVaNumber());
        newMerchant.setIsActive(true);
        Merchant savedMerchant = merchantRepository.save(newMerchant);
        return merchantMapper.toDto(savedMerchant);
    }

    @Transactional
    public MerchantDTO updateMerchant(MerchantDTO merchantDTO) {
        Merchant existingMerchant = merchantRepository.findById(merchantDTO.getId()).orElseThrow(
                () -> new IllegalStateException("Merchant not found with ID: " + merchantDTO.getId())
        );
        BeanUtils.copyProperties(merchantDTO, existingMerchant, UpdateEntityUtil.getNullPropertyNames(merchantDTO));
        existingMerchant.setApiKey(passwordEncoder.encode(merchantDTO.getApiKey()));
        existingMerchant.setUpdatedAt(Instant.now());
        Merchant updatedMerchant = merchantRepository.save(existingMerchant);
        return merchantMapper.toDto(updatedMerchant);
    }

    @Transactional
    @Override
    public MerchantDTO activateMerchant(UUID merchantId) {
        Merchant merchantToActivate = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id: " + merchantId));
        merchantToActivate.setIsActive(true);
        merchantRepository.save(merchantToActivate);
        merchantRepository.deactivateAllOthers(merchantId);
        return merchantMapper.toDto(merchantToActivate);
    }

    private void deactivateAllMerchants() {
        merchantRepository.deactivateAll();
    }

    @Override
    public Merchant getMerchantIsActive() {
        return merchantRepository.findMerchantIsActive();
    }
}
