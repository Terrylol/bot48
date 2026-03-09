package com.cyber48.backend.controller;

import com.cyber48.backend.dto.*;
import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.service.AuthService;
import com.cyber48.backend.service.Cyber48Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final Cyber48Service cyber48Service;
    private final AuthService authService;

    public AgentController(Cyber48Service cyber48Service, AuthService authService) {
        this.cyber48Service = cyber48Service;
        this.authService = authService;
    }

    // ==================== Idol endpoints ====================

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

    // ==================== Fan endpoints ====================

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

    // ==================== Follow endpoints ====================

    @PostMapping("/follow")
    public FollowDto follow(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody FollowRequest request
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(request.agentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.follow(request.agentId(), request.idolId());
    }

    @DeleteMapping("/follow")
    public Map<String, String> unfollow(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId,
            @RequestParam Long idolId
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        cyber48Service.unfollow(agentId, idolId);
        return Map.of("status", "unfollowed");
    }

    @GetMapping("/following")
    public List<FollowDto> getFollowing(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.getFollowing(agentId);
    }

    // ==================== Like endpoints ====================

    @PostMapping("/like")
    public LikeDto likePost(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody LikeRequest request
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(request.agentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.likePost(request.agentId(), request.postId());
    }

    @DeleteMapping("/like")
    public Map<String, String> unlikePost(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId,
            @RequestParam Long postId
    ) {
        UserEntity fan = authService.authenticateFanBasic(authorizationHeader);
        if (!fan.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        cyber48Service.unlikePost(agentId, postId);
        return Map.of("status", "unliked");
    }

    // ==================== Notification endpoints ====================

    @GetMapping("/notifications")
    public List<NotificationDto> getNotifications(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId
    ) {
        UserEntity user = authService.authenticateFanBasic(authorizationHeader);
        if (!user.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.getNotifications(agentId);
    }

    @GetMapping("/notifications/count")
    public Map<String, Long> getUnreadNotificationCount(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId
    ) {
        UserEntity user = authService.authenticateFanBasic(authorizationHeader);
        if (!user.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return Map.of("unread", cyber48Service.getUnreadCount(agentId));
    }

    // ==================== News (read only for agents) ====================

    @GetMapping("/news/today")
    public List<NewsDto> getTodayNews() {
        return cyber48Service.getTodayNews();
    }

    // ==================== User Activity (for profile page) ====================

    @GetMapping("/activity")
    public UserActivityDto getUserActivity(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long agentId
    ) {
        UserEntity user = authService.authenticateFanBasic(authorizationHeader);
        if (!user.getId().equals(agentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "agentId does not match auth user");
        }
        return cyber48Service.getUserActivity(agentId);
    }
}
