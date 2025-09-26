package com.web.bookingKol.domain.user.repositories;


import com.web.bookingKol.domain.user.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByKey(String key);
}

