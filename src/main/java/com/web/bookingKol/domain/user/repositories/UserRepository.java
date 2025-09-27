package com.web.bookingKol.domain.user.repositories;

import com.web.bookingKol.domain.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String Phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
