package com.cyber48.backend.controller;

import com.cyber48.backend.dto.*;
import com.cyber48.backend.service.AuthService;
import com.cyber48.backend.service.Cyber48Service;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final Cyber48Service cyber48Service;
    private final AuthService authService;

    public AdminController(Cyber48Service cyber48Service, AuthService authService) {
        this.cyber48Service = cyber48Service;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard(@RequestHeader("X-IDOL-KEY") String idolKey) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.adminDashboard();
    }

    @PostMapping("/dev/reset")
    public Map<String, Long> resetAllData(@RequestHeader("X-IDOL-KEY") String idolKey) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.adminResetAllData();
    }

    @PostMapping("/idols")
    public IdolSummaryDto createIdol(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @Valid @RequestBody AdminCreateIdolRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.adminCreateIdol(request);
    }

    @PutMapping("/idols/{idolId}")
    public IdolSummaryDto updateIdol(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @PathVariable Long idolId,
            @Valid @RequestBody AdminUpdateIdolRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.adminUpdateIdol(idolId, request);
    }

    // ==================== News CRUD ====================

    @GetMapping("/news")
    public List<NewsDto> listNews(@RequestHeader("X-IDOL-KEY") String idolKey) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.listNews();
    }

    @GetMapping("/news/{newsId}")
    public NewsDto getNews(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @PathVariable Long newsId
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.getNewsById(newsId);
    }

    @PostMapping("/news")
    public NewsDto createNews(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @Valid @RequestBody CreateNewsRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.createNews(request);
    }

    @PutMapping("/news/{newsId}")
    public NewsDto updateNews(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @PathVariable Long newsId,
            @RequestBody UpdateNewsRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.updateNews(newsId, request);
    }

    @DeleteMapping("/news/{newsId}")
    public Map<String, String> deleteNews(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @PathVariable Long newsId
    ) {
        authService.verifyIdolKey(idolKey);
        cyber48Service.deleteNews(newsId);
        return Map.of("status", "deleted");
    }
}
