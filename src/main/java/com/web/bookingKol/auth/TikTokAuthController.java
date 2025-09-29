package com.web.bookingKol.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Controller
public class TikTokAuthController {
    @Value("${CLIENT_KEY}")
    private String CLIENT_KEY;
    @Value("${CLIENT_SECRET}")
    private String CLIENT_SECRET;
    @Value("${REDIRECT_URI}")
    private String REDIRECT_URI;

    // B1: Redirect sang TikTok Login
    @GetMapping("/auth/tiktok/login")
    public String login(HttpSession session) throws Exception {
        // Tạo code_verifier
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);

        // Tạo code_challenge
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);

        // Lưu code_verifier vào session để dùng ở bước 2
        session.setAttribute("code_verifier", codeVerifier);

        // Redirect sang TikTok
        String url = "https://www.tiktok.com/v2/auth/authorize/"
                + "?client_key=" + CLIENT_KEY
                + "&scope=user.info.basic"
                + "&response_type=code"
                + "&redirect_uri=" + REDIRECT_URI
                + "&state=abc123"
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
        return "redirect:" + url;
    }

    // B2: Callback từ TikTok
    @GetMapping("/auth/tiktok/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session) {

        String codeVerifier = (String) session.getAttribute("code_verifier");
        if (codeVerifier == null) {
            return ResponseEntity.badRequest().body("Missing code_verifier");
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_key", CLIENT_KEY);
        body.add("client_secret", CLIENT_SECRET);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", REDIRECT_URI);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://open.tiktokapis.com/v2/oauth/token/",
                request,
                Map.class
        );

        System.out.println("TikTok token response: " + response.getBody());
        return ResponseEntity.ok(response.getBody());
    }
}
