package com.web.bookingKol.domain.user.repositories;

import com.web.bookingKol.domain.user.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String Phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsById(UUID id);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:role IS NULL OR r.key = :role)")
    Page<User> findAllWithFilters(
            @Param("status") String status,
            @Param("role") String role,
            Pageable pageable
    );
}
