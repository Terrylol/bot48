package com.cyber48.backend.service;

import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.entity.UserRole;
import com.cyber48.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String idolAgentKey;

    public AuthService(
            UserRepository userRepository,
            @Value("${security.idol-agent-key:idol-dev-key}") String idolAgentKey
    ) {
        this.userRepository = userRepository;
        this.idolAgentKey = idolAgentKey;
    }

    public void verifyIdolKey(String key) {
        if (key == null || key.isBlank() || !idolAgentKey.equals(key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid idol key");
        }
    }

    public UserEntity authenticateFanBasic(String authorizationHeader) {
        Credentials credentials = decodeBasic(authorizationHeader);
        UserEntity user = authenticateByCredentials(credentials);
        if (user.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "fan role required");
        }
        return user;
    }

    public UserEntity authenticateAnyBasic(String authorizationHeader) {
        Credentials credentials = decodeBasic(authorizationHeader);
        return authenticateByCredentials(credentials);
    }

    private UserEntity authenticateByCredentials(Credentials credentials) {
        UserEntity user = userRepository.findByUsername(credentials.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(credentials.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return user;
    }

    public UserEntity authenticateAny(LoginCredentials loginCredentials) {
        UserEntity user = userRepository.findByUsername(loginCredentials.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(loginCredentials.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return user;
    }

    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    private Credentials decodeBasic(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing basic auth");
        }
        try {
            String encoded = authorizationHeader.substring("Basic ".length()).trim();
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            int split = decoded.indexOf(':');
            if (split <= 0) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid basic auth format");
            }
            String username = decoded.substring(0, split);
            String password = decoded.substring(split + 1);
            if (username.isBlank() || password.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid basic auth credentials");
            }
            return new Credentials(username, password);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid basic auth token");
        }
    }

    public record LoginCredentials(String username, String password) {
    }

    private record Credentials(String username, String password) {
    }
}
