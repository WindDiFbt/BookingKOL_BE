package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.user.dtos.AdminAccountDTO;
import com.web.bookingKol.domain.user.services.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superadmin/account")
public class SuperAdminAccountController {
    @Autowired
    private AdminUserService adminUserService;

    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdminAccount(@RequestBody @Valid AdminAccountDTO adminAccountDTO) {
        return ResponseEntity.ok(adminUserService.createAdminAccount(adminAccountDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAccountWithFilter(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(adminUserService.getAllAccountWithFilter(pageable, status, role));
    }
}
