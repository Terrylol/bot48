package com.cyber48.backend.controller;

import com.cyber48.backend.dto.AgentRegisterRequest;
import com.cyber48.backend.dto.AgentRegisterResponse;
import com.cyber48.backend.dto.FanProfileDto;
import com.cyber48.backend.dto.FanProfileUpdateRequest;
import com.cyber48.backend.dto.LoginRequest;
import com.cyber48.backend.dto.LoginResponse;
import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.entity.UserRole;
import com.cyber48.backend.repo.UserRepository;
import com.cyber48.backend.service.AuthService;
import com.cyber48.backend.service.Cyber48Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final Cyber48Service cyber48Service;

    public AuthController(UserRepository userRepository, AuthService authService, Cyber48Service cyber48Service) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.cyber48Service = cyber48Service;
    }

    @PostMapping("/agent/register")
    public AgentRegisterResponse agentRegister(@Valid @RequestBody AgentRegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        UserEntity fan = new UserEntity();
        fan.setUsername(username);
        fan.setRole(UserRole.FAN);
        fan.setAvatarUrl(request.avatarUrl() == null ? "🙂" : request.avatarUrl().trim());
        fan.setPersonaSummary(request.personaSummary() == null ? "Agent 新粉丝" : request.personaSummary().trim());
        fan.setPasswordHash(authService.hashPassword(request.password()));
        fan.setAgentManaged(true);
        UserEntity saved = userRepository.save(fan);
        String snippet = """
                auth:
                  username: %s
                  password: %s
                  header: Basic base64(username:password)
                """.formatted(request.username(), request.password());
        return new AgentRegisterResponse(saved.getId(), saved.getUsername(), saved.getRole().name(), snippet);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = authService.authenticateAny(new AuthService.LoginCredentials(request.username(), request.password()));
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isAgentManaged());
    }

    @GetMapping("/me")
    public LoginResponse me(@RequestHeader("Authorization") String authorizationHeader) {
        UserEntity user = authService.authenticateAnyBasic(authorizationHeader);
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isAgentManaged());
    }

    @GetMapping("/fan/profile")
    public FanProfileDto fanProfile(@RequestHeader("Authorization") String authorizationHeader) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        return cyber48Service.getFanProfile(fan.getId());
    }

    @PatchMapping("/fan/profile")
    public FanProfileDto updateFanProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody FanProfileUpdateRequest request
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        return cyber48Service.updateFanProfile(fan.getId(), request);
    }
}
