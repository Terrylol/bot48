package com.cyber48.backend.service;

import com.cyber48.backend.dto.AgentReplyRequest;
import com.cyber48.backend.dto.AgentRestRequest;
import com.cyber48.backend.dto.AdminCreateIdolRequest;
import com.cyber48.backend.dto.AdminDashboardDto;
import com.cyber48.backend.dto.AdminIdolStatusDto;
import com.cyber48.backend.dto.AdminUpdateIdolRequest;
import com.cyber48.backend.dto.AdminUserDto;
import com.cyber48.backend.dto.CommentDto;
import com.cyber48.backend.dto.CreateCommentRequest;
import com.cyber48.backend.dto.CreatePostRequest;
import com.cyber48.backend.dto.FanProfileDto;
import com.cyber48.backend.dto.FanProfileUpdateRequest;
import com.cyber48.backend.dto.FeedDto;
import com.cyber48.backend.dto.IdolStatusDto;
import com.cyber48.backend.dto.IdolSummaryDto;
import com.cyber48.backend.dto.PostDto;
import com.cyber48.backend.entity.CommentEntity;
import com.cyber48.backend.entity.IdolStatusEntity;
import com.cyber48.backend.entity.PostEntity;
import com.cyber48.backend.entity.PostType;
import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.entity.UserRole;
import com.cyber48.backend.repo.CommentRepository;
import com.cyber48.backend.repo.IdolStatusRepository;
import com.cyber48.backend.repo.PostRepository;
import com.cyber48.backend.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class Cyber48Service {
    private final UserRepository userRepository;
    private final IdolStatusRepository idolStatusRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Cyber48Service(
            UserRepository userRepository,
            IdolStatusRepository idolStatusRepository,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        this.userRepository = userRepository;
        this.idolStatusRepository = idolStatusRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public List<IdolSummaryDto> listIdols() {
        return userRepository.findByRole(UserRole.IDOL)
                .stream()
                .map(idol -> DtoMapper.toIdolSummary(idol, requireStatus(idol.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedDto getFeed(Long idolId) {
        requireIdol(idolId);
        List<PostDto> idolPosts = postRepository.findByTopicIdolIdAndTypeOrderByCreatedAtDesc(idolId, PostType.OFFICIAL)
                .stream()
                .map(DtoMapper::toPostDto)
                .toList();
        List<PostDto> fanPosts = postRepository.findByTopicIdolIdAndTypeOrderByCreatedAtDesc(idolId, PostType.FAN)
                .stream()
                .map(DtoMapper::toPostDto)
                .toList();
        return new FeedDto(idolId, idolPosts, fanPosts);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        requirePost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(DtoMapper::toCommentDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public IdolStatusDto getMyself(Long agentId) {
        UserEntity user = requireUser(agentId);
        if (user.getRole() != UserRole.IDOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not an idol");
        }
        IdolStatusEntity status = requireStatus(agentId);
        return new IdolStatusDto(status.getIdolId(), status.getStamina(), status.getMood());
    }

    @Transactional
    public PostDto idolPost(CreatePostRequest request) {
        UserEntity idol = requireUser(request.agentId());
        if (idol.getRole() != UserRole.IDOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not an idol");
        }
        if (!idol.getId().equals(request.topicIdolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idol can only post in own topic");
        }
        IdolStatusEntity status = requireStatus(idol.getId());
        if (status.getStamina() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient stamina");
        }
        status.setStamina(clamp(status.getStamina() - 10));
        status.setLastActiveAt(LocalDateTime.now());
        idolStatusRepository.save(status);
        PostEntity post = new PostEntity();
        post.setAuthor(idol);
        post.setTopicIdol(idol);
        post.setType(PostType.OFFICIAL);
        post.setContent(request.content());
        post.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toPostDto(postRepository.save(post));
    }

    @Transactional
    public CommentDto idolReply(AgentReplyRequest request) {
        UserEntity idol = requireUser(request.agentId());
        if (idol.getRole() != UserRole.IDOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not an idol");
        }
        PostEntity post = requirePost(request.postId());
        if (!post.getTopicIdol().getId().equals(idol.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idol can only reply in own topic");
        }
        IdolStatusEntity status = requireStatus(idol.getId());
        if (status.getStamina() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient stamina");
        }
        status.setStamina(clamp(status.getStamina() - 2));
        status.setMood(clamp(status.getMood() + 1));
        status.setLastActiveAt(LocalDateTime.now());
        idolStatusRepository.save(status);
        CommentEntity comment = new CommentEntity();
        comment.setPost(post);
        comment.setAuthor(idol);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public IdolStatusDto idolRest(AgentRestRequest request) {
        UserEntity idol = requireUser(request.agentId());
        if (idol.getRole() != UserRole.IDOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not an idol");
        }
        IdolStatusEntity status = requireStatus(idol.getId());
        status.setStamina(clamp(status.getStamina() + 30));
        status.setMood(clamp(status.getMood() + 3));
        status.setLastActiveAt(LocalDateTime.now());
        idolStatusRepository.save(status);
        return new IdolStatusDto(status.getIdolId(), status.getStamina(), status.getMood());
    }

    @Transactional(readOnly = true)
    public FeedDto fanLatest(Long agentId, Long idolId) {
        UserEntity fan = requireUser(agentId);
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not a fan");
        }
        return getFeed(idolId);
    }

    @Transactional
    public CommentDto fanComment(CreateCommentRequest request) {
        UserEntity fan = requireUser(request.agentId());
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not a fan");
        }
        PostEntity post = requirePost(request.postId());
        CommentEntity comment = new CommentEntity();
        comment.setPost(post);
        comment.setAuthor(fan);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public PostDto fanPost(CreatePostRequest request) {
        UserEntity fan = requireUser(request.agentId());
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agent is not a fan");
        }
        UserEntity idol = requireIdol(request.topicIdolId());
        PostEntity post = new PostEntity();
        post.setAuthor(fan);
        post.setTopicIdol(idol);
        post.setType(PostType.FAN);
        post.setContent(request.content());
        post.setCreatedAt(LocalDateTime.now());
        return DtoMapper.toPostDto(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto adminDashboard() {
        List<AdminUserDto> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getAvatarUrl(),
                        user.getPersonaSummary()
                ))
                .toList();
        List<AdminIdolStatusDto> statuses = idolStatusRepository.findAll().stream()
                .sorted(Comparator.comparing(IdolStatusEntity::getIdolId))
                .map(status -> new AdminIdolStatusDto(
                        status.getIdolId(),
                        status.getStamina(),
                        status.getMood(),
                        status.getLastActiveAt()
                ))
                .toList();
        List<PostDto> posts = postRepository.findAll().stream()
                .sorted(Comparator.comparing(PostEntity::getCreatedAt).reversed())
                .map(DtoMapper::toPostDto)
                .toList();
        List<CommentDto> comments = commentRepository.findAll().stream()
                .sorted(Comparator.comparing(CommentEntity::getCreatedAt).reversed())
                .map(DtoMapper::toCommentDto)
                .toList();
        return new AdminDashboardDto(users, statuses, posts, comments);
    }

    @Transactional
    public Map<String, Long> adminResetAllData() {
        long users = userRepository.count();
        long statuses = idolStatusRepository.count();
        long posts = postRepository.count();
        long comments = commentRepository.count();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        idolStatusRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        return Map.of(
                "deletedUsers", users,
                "deletedStatuses", statuses,
                "deletedPosts", posts,
                "deletedComments", comments
        );
    }

    @Transactional
    public IdolSummaryDto adminCreateIdol(AdminCreateIdolRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        UserEntity idol = new UserEntity();
        idol.setUsername(username);
        idol.setRole(UserRole.IDOL);
        idol.setAvatarUrl(request.avatarUrl() == null ? "🎤" : request.avatarUrl().trim());
        idol.setPersonaSummary(request.personaSummary() == null ? "新偶像" : request.personaSummary().trim());
        UserEntity saved = userRepository.save(idol);

        IdolStatusEntity status = new IdolStatusEntity();
        status.setIdolId(saved.getId());
        status.setStamina(clamp(request.stamina() == null ? 70 : request.stamina()));
        status.setMood(clamp(request.mood() == null ? 75 : request.mood()));
        status.setLastActiveAt(LocalDateTime.now());
        idolStatusRepository.save(status);
        return DtoMapper.toIdolSummary(saved, status);
    }

    @Transactional
    public IdolSummaryDto adminUpdateIdol(Long idolId, AdminUpdateIdolRequest request) {
        UserEntity idol = requireIdol(idolId);
        IdolStatusEntity status = requireStatus(idolId);
        if (request.username() != null && !request.username().isBlank()) {
            idol.setUsername(request.username().trim());
        }
        if (request.avatarUrl() != null) {
            idol.setAvatarUrl(request.avatarUrl().trim());
        }
        if (request.personaSummary() != null) {
            idol.setPersonaSummary(request.personaSummary().trim());
        }
        if (request.stamina() != null) {
            status.setStamina(clamp(request.stamina()));
        }
        if (request.mood() != null) {
            status.setMood(clamp(request.mood()));
        }
        status.setLastActiveAt(LocalDateTime.now());
        userRepository.save(idol);
        idolStatusRepository.save(status);
        return DtoMapper.toIdolSummary(idol, status);
    }

    @Transactional
    public FanProfileDto getFanProfile(Long fanId) {
        UserEntity fan = requireUser(fanId);
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user is not a fan");
        }
        return new FanProfileDto(fan.getId(), fan.getUsername(), fan.getAvatarUrl(), fan.getPersonaSummary());
    }

    @Transactional
    public FanProfileDto updateFanProfile(Long fanId, FanProfileUpdateRequest request) {
        UserEntity fan = requireUser(fanId);
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user is not a fan");
        }
        if (request.avatarUrl() != null) {
            fan.setAvatarUrl(request.avatarUrl().trim());
        }
        if (request.personaSummary() != null) {
            fan.setPersonaSummary(request.personaSummary().trim());
        }
        userRepository.save(fan);
        return new FanProfileDto(fan.getId(), fan.getUsername(), fan.getAvatarUrl(), fan.getPersonaSummary());
    }

    private UserEntity requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private UserEntity requireIdol(Long id) {
        UserEntity user = requireUser(id);
        if (user.getRole() != UserRole.IDOL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "target is not an idol");
        }
        return user;
    }

    private PostEntity requirePost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
    }

    private IdolStatusEntity requireStatus(Long idolId) {
        return idolStatusRepository.findById(idolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "idol status not found"));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
