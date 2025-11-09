package com.web.bookingKol.domain.file.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {
    @Value("${SUPABASE_ANON_URL}")
    private String SUPABASE_ANON_URL;

    @Value("${SUPABASE_SERVICE_ROLE_KEY}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${STORAGE_BUCKET}")
    private String STORAGE_BUCKET;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadFile(MultipartFile file, String fileName, String contentType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY);
            headers.setContentType(MediaType.parseMediaType(contentType));
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            String url = SUPABASE_ANON_URL + "/storage/v1/object/" + STORAGE_BUCKET + "/" + fileName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Upload failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error uploading file to Supabase: " + e.getMessage());
        }
        return SUPABASE_ANON_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + fileName;
    }

}
