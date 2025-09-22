package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> mailOpt = userRepository.findByEmail(identifier);
        if (mailOpt.isPresent()) {
            return new UserDetailsImpl(mailOpt.get());
        }
        Optional<User> phoneOpt = userRepository.findByPhone(identifier);
        if (phoneOpt.isPresent()) {
            return new UserDetailsImpl(phoneOpt.get());
        }
        throw new UsernameNotFoundException("User not found!");
    }

    public UserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserDetailsImpl(user);
    }
}
