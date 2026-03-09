package com.cyber48.backend.controller;

import com.cyber48.backend.dto.*;
import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.entity.UserRole;
import com.cyber48.backend.repo.UserRepository;
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
    private final UserRepository userRepository;

    public AdminController(Cyber48Service cyber48Service, AuthService authService, UserRepository userRepository) {
        this.cyber48Service = cyber48Service;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard(@RequestHeader("X-IDOL-KEY") String idolKey) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.adminDashboard();
    }

    @GetMapping("/users")
    public List<AdminUserDto> listUsers(@RequestHeader("X-IDOL-KEY") String idolKey) {
        authService.verifyIdolKey(idolKey);
        return userRepository.findAll().stream().map(u -> new AdminUserDto(u.getId(), u.getUsername(), u.getRole().name(), u.getAvatarUrl(), u.getPersonaSummary())).toList();
    }

    @PostMapping("/fans")
    public Map<String, Object> createFan(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @RequestBody Map<String, String> body
    ) {
        authService.verifyIdolKey(idolKey);
        String username = body.get("username");
        String password = body.getOrDefault("password", "123456");
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        UserEntity fan = new UserEntity();
        fan.setUsername(username);
        fan.setRole(UserRole.FAN);
        fan.setAvatarUrl(body.getOrDefault("avatar", "🙂"));
        fan.setPersonaSummary(body.getOrDefault("persona", "新粉丝"));
        fan.setPasswordHash(authService.hashPassword(password));
        fan.setAgentManaged(true);
        UserEntity saved = userRepository.save(fan);
        return Map.of("id", saved.getId(), "username", saved.getUsername(), "password", password, "message", "创建成功");
    }

    @PutMapping("/fans/{fanId}/password")
    public Map<String, Object> resetFanPassword(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @PathVariable Long fanId,
            @RequestBody Map<String, String> body
    ) {
        authService.verifyIdolKey(idolKey);
        String newPassword = body.getOrDefault("password", "123456");
        UserEntity fan = userRepository.findById(fanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not a fan user");
        }
        fan.setPasswordHash(authService.hashPassword(newPassword));
        userRepository.save(fan);
        return Map.of("id", fan.getId(), "username", fan.getUsername(), "password", newPassword, "message", "密码重置成功");
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
