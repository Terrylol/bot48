package com.cyber48.backend.controller;

import com.cyber48.backend.dto.AdminDashboardDto;
import com.cyber48.backend.dto.AdminCreateIdolRequest;
import com.cyber48.backend.dto.AdminUpdateIdolRequest;
import com.cyber48.backend.dto.IdolSummaryDto;
import com.cyber48.backend.service.AuthService;
import com.cyber48.backend.service.Cyber48Service;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
