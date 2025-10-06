package com.web.bookingKol.auth;

import com.web.bookingKol.domain.user.repositories.BlacklistedTokenRepository;
import com.web.bookingKol.domain.user.services.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String accessToken = getToken(request);
            logger.info(">>> TOKEN: " + accessToken);
            if (accessToken != null && jwtUtils.validateAccessToken(accessToken)) {
                if (blacklistedTokenRepository.existsByToken(accessToken)) {
                    logger.warn("Token đã bị thu hồi (logout)");
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID userId = jwtUtils.claimUserId(accessToken);
                logger.info(">>> USERID FROM TOKEN: " + userId);
                UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
                logger.info(">>> AUTHORITIES: " + userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn(">>> TOKEN INVALID");
            }
        } catch (Exception e) {
            logger.error(">>> ERROR JWT FILTER: ", e);
        }
        filterChain.doFilter(request, response);
    }


    private String getToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
