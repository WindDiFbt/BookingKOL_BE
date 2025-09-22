package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserRestController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.ok().body(userService.getAllUser());
    }
}
