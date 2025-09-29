package com.web.bookingKol.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
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
                + "&redirect_uri=" + REDIRECT_URI + "/api/v1/auth/tiktok/callback"
                + "&state=abc123"
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
        return "redirect:" + url;
    }

    // B2: Callback từ TikTok
    @GetMapping("/auth/tiktok/callback")
    public String callback(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           HttpSession session) {

        String codeVerifier = (String) session.getAttribute("code_verifier");

        String tokenUrl = "https://open.tiktokapis.com/v2/oauth/token/";

        RestTemplate rest = new RestTemplate();
        Map<String, String> body = Map.of(
                "client_key", CLIENT_KEY,
                "client_secret", CLIENT_SECRET,
                "code", code,
                "grant_type", "authorization_code",
                "redirect_uri", REDIRECT_URI,
                "code_verifier", codeVerifier
        );

        Map response = rest.postForObject(tokenUrl, body, Map.class);

        System.out.println("TikTok token response: " + response);

        return "redirect:/success";
    }
}
