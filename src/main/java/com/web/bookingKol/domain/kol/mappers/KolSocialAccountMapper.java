package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.KolSocialAccountDTO;
import com.web.bookingKol.domain.kol.models.KolSocialAccount;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KolSocialAccountMapper {
    KolSocialAccountDTO toDto(KolSocialAccount kolSocialAccount);

    List<KolSocialAccountDTO> toDtoList(List<KolSocialAccount> kolSocialAccounts);
}
