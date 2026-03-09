package com.cyber48.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageDownloadService {
    private static final Logger log = LoggerFactory.getLogger(ImageDownloadService.class);

    private final Path uploadDir;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 匹配 meituan.net 图片域名的正则
    private static final Pattern MEITUAN_PATTERN = Pattern.compile("(https?://[^/]+\\.meituan\\.net/[^\\s]+)");

    public ImageDownloadService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", this.uploadDir, e);
        }
    }

    /**
     * 下载远程图片到本地存储，返回本地路径
     * 如果不是远程图片或下载失败，返回原 URL
     */
    public String downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        // 如果已经是本地路径，直接返回
        if (imageUrl.startsWith("/uploads/")) {
            return imageUrl;
        }

        // 检查是否是支持的远程图片域名
        Matcher matcher = MEITUAN_PATTERN.matcher(imageUrl);
        if (!matcher.find()) {
            log.debug("Image URL not from supported domain, keeping original: {}", imageUrl);
            return imageUrl;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                log.warn("Failed to download image, HTTP {}: {}", response.statusCode(), imageUrl);
                return imageUrl;
            }

            // 生成文件名
            String ext = getExtension(imageUrl);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "img_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 保存文件
            Path target = uploadDir.resolve(fileName);
            Files.write(target, response.body());

            String localPath = "/uploads/" + fileName;
            log.info("Downloaded image to: {}", localPath);
            return localPath;

        } catch (Exception e) {
            log.error("Failed to download image: {}", imageUrl, e);
            return imageUrl;
        }
    }

    private String getExtension(String url) {
        // 从 URL 中提取文件扩展名
        int idx = url.lastIndexOf('.');
        if (idx > 0 && idx < url.length() - 1) {
            String ext = url.substring(idx);
            // 确保是图片扩展名
            if (ext.matches("\\.(png|jpg|jpeg|gif|webp)")) {
                return ext;
            }
        }
        return ".png"; // 默认 PNG
    }
}
