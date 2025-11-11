package com.web.bookingKol.auth;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.user.models.Role;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.RoleRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Component
public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtils jwtUtils;
    private static final String FRONTEND_REDIRECT_URL = "http://127.0.0.1:5500/oauth-redirect.html";

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(oauthUser.getAttribute("name"));
            newUser.setAvatarUrl(oauthUser.getAttribute("picture"));
            newUser.setStatus(Enums.UserStatus.ACTIVE.name());
            newUser.setCreatedAt(Instant.now());
            Role role = roleRepository.findByKey(Enums.Roles.USER.name())
                    .orElseThrow(() -> new RuntimeException("Role không tìm thấy"));
            newUser.setRoles(Set.of(role));
            return userRepository.saveAndFlush(newUser);
        });
        String jwtToken = jwtUtils.generateAccessTokenUser(user);
        String redirectUrl = UriComponentsBuilder.fromUriString(FRONTEND_REDIRECT_URL)
                .queryParam("token", jwtToken)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

