package com.cyber48.backend.controller;

import com.cyber48.backend.dto.AgentReplyRequest;
import com.cyber48.backend.dto.AgentRestRequest;
import com.cyber48.backend.dto.CommentDto;
import com.cyber48.backend.dto.CreateCommentRequest;
import com.cyber48.backend.dto.CreatePostRequest;
import com.cyber48.backend.dto.FeedDto;
import com.cyber48.backend.dto.IdolStatusDto;
import com.cyber48.backend.dto.PostDto;
import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.service.AuthService;
import com.cyber48.backend.service.Cyber48Service;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final Cyber48Service cyber48Service;
    private final AuthService authService;

    public AgentController(Cyber48Service cyber48Service, AuthService authService) {
        this.cyber48Service = cyber48Service;
        this.authService = authService;
    }

    @GetMapping("/myself")
    public IdolStatusDto myself(@RequestHeader("X-IDOL-KEY") String idolKey, @RequestParam Long agentId) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.getMyself(agentId);
    }

    @PostMapping("/post")
    public PostDto idolPost(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @Valid @RequestBody CreatePostRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.idolPost(request);
    }

    @PostMapping("/reply")
    public CommentDto idolReply(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @Valid @RequestBody AgentReplyRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.idolReply(request);
    }

    @PostMapping("/rest")
    public IdolStatusDto idolRest(
            @RequestHeader("X-IDOL-KEY") String idolKey,
            @Valid @RequestBody AgentRestRequest request
    ) {
        authService.verifyIdolKey(idolKey);
        return cyber48Service.idolRest(request);
    }

    @GetMapping("/feed/latest")
    public FeedDto fanLatest(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId,
            @RequestParam Long idolId
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.fanLatest(agentId, idolId);
    }

    @PostMapping("/comment")
    public CommentDto fanComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(request.agentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.fanComment(request);
    }

    @PostMapping("/fan-post")
    public PostDto fanPost(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreatePostRequest request
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(request.agentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.fanPost(request);
    }
}
