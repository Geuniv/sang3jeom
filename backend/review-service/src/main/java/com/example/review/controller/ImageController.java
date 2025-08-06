package com.example.review.controller;

import com.example.review.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@Slf4j
@RequiredArgsConstructor
public class ImageController {

    private final ReviewImageService reviewImageService;

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestBody Map<String, String> payload) {
        log.info("Received request for presigned url {}", payload.get("filename"));
        String filename = payload.get("filename");
        if (filename == null || filename.isBlank()) {
            Map<String, String> errorResponse = Map.of("error", "filename is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // 실제 S3 Presigned URL 생성 시도
            String presignedUrl = reviewImageService.getPresignedUrl(filename);
            log.info("S3 Presigned URL 생성 성공: {}", presignedUrl);
            
            // JSON 응답 형식으로 반환
            Map<String, String> response = Map.of(
                "presignedUrl", presignedUrl,
                "filename", filename
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("S3 Presigned URL 생성 실패", e);
            // 실패시 더미 URL 반환
            String dummyUrl = "https://via.placeholder.com/300x200?text=Upload+Failed";
            Map<String, String> response = Map.of(
                "presignedUrl", dummyUrl,
                "filename", filename
            );
            return ResponseEntity.ok(response);
        }
    }
}
