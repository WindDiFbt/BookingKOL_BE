package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.KolProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface KolProfileRepository extends JpaRepository<KolProfile, UUID> {
    @Query("SELECT k FROM KolProfile k WHERE k.user.id = :userId")
    Optional<KolProfile> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT k FROM KolProfile k WHERE k.id = :kolId")
    Optional<KolProfile> findByKolId(@Param("kolId") UUID kolId);
}
