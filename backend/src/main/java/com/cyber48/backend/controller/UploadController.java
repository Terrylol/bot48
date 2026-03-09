package com.cyber48.backend.controller;

import com.cyber48.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {
    private final AuthService authService;
    private final String uploadDir;

    public UploadController(
            AuthService authService,
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.authService = authService;
        this.uploadDir = uploadDir;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @RequestParam("file") MultipartFile file
    ) {
        authService.verifyIdolKey(idolKey);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only image files are allowed");
        }

        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(basePath);

            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String ext = "";
            int idx = originalName.lastIndexOf('.');
            if (idx >= 0) {
                ext = originalName.substring(idx);
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "img_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path target = basePath.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "upload failed");
        }
    }
}
