package com.cyber48.backend.service;

import com.cyber48.backend.dto.*;
import com.cyber48.backend.entity.*;
import com.cyber48.backend.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    private final NewsRepository newsRepository;

    public Cyber48Service(
            UserRepository userRepository,
            IdolStatusRepository idolStatusRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            FollowRepository followRepository,
            LikeRepository likeRepository,
            NotificationRepository notificationRepository,
            NewsRepository newsRepository
    ) {
        this.userRepository = userRepository;
        this.idolStatusRepository = idolStatusRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.likeRepository = likeRepository;
        this.notificationRepository = notificationRepository;
        this.newsRepository = newsRepository;
    }

    // ==================== Existing: Idol listing / feed / comments ====================

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
                .stream().map(DtoMapper::toPostDto).toList();
        List<PostDto> fanPosts = postRepository.findByTopicIdolIdAndTypeOrderByCreatedAtDesc(idolId, PostType.FAN)
                .stream().map(DtoMapper::toPostDto).toList();
        return new FeedDto(idolId, idolPosts, fanPosts);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        requirePost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(DtoMapper::toCommentDto).toList();
    }

    // ==================== Existing: Idol agent actions ====================

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
        PostEntity saved = postRepository.save(post);

        // Notify followers about new post
        notifyFollowers(idol.getId(), saved);

        return DtoMapper.toPostDto(saved);
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
        CommentEntity saved = commentRepository.save(comment);

        // Notify post author if it's a fan post, or notify all comment authors on this post
        notifyReply(idol, post, saved);

        return DtoMapper.toCommentDto(saved);
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

    // ==================== Existing: Fan agent actions ====================

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
        CommentEntity saved = commentRepository.save(comment);

        // Notify post author about comment
        if (!post.getAuthor().getId().equals(fan.getId())) {
            createNotification(post.getAuthor().getId(), NotificationType.COMMENT,
                    fan.getId(), post.getId(), saved.getId());
        }

        return DtoMapper.toCommentDto(saved);
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

    // ==================== NEW: Follow ====================

    @Transactional
    public FollowDto follow(Long fanId, Long idolId) {
        UserEntity fan = requireUser(fanId);
        if (fan.getRole() != UserRole.FAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only fans can follow");
        }
        requireIdol(idolId);
        if (followRepository.existsByFanIdAndIdolId(fanId, idolId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already following");
        }
        FollowEntity follow = new FollowEntity();
        follow.setFanId(fanId);
        follow.setIdolId(idolId);
        follow.setCreatedAt(LocalDateTime.now());
        FollowEntity saved = followRepository.save(follow);
        return new FollowDto(saved.getId(), saved.getFanId(), saved.getIdolId(), saved.getCreatedAt());
    }

    @Transactional
    public void unfollow(Long fanId, Long idolId) {
        if (!followRepository.existsByFanIdAndIdolId(fanId, idolId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not following");
        }
        followRepository.deleteByFanIdAndIdolId(fanId, idolId);
    }

    @Transactional(readOnly = true)
    public List<FollowDto> getFollowing(Long fanId) {
        return followRepository.findByFanId(fanId).stream()
                .map(f -> new FollowDto(f.getId(), f.getFanId(), f.getIdolId(), f.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long idolId) {
        return followRepository.countByIdolId(idolId);
    }

    // ==================== NEW: Like ====================

    @Transactional
    public LikeDto likePost(Long userId, Long postId) {
        requireUser(userId);
        requirePost(postId);
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already liked");
        }
        LikeEntity like = new LikeEntity();
        like.setUserId(userId);
        like.setPostId(postId);
        like.setCreatedAt(LocalDateTime.now());
        LikeEntity saved = likeRepository.save(like);

        // Notify post author
        PostEntity post = requirePost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            createNotification(post.getAuthor().getId(), NotificationType.LIKE,
                    userId, postId, null);
        }

        return new LikeDto(saved.getId(), saved.getUserId(), saved.getPostId(), saved.getCreatedAt());
    }

    @Transactional
    public void unlikePost(Long userId, Long postId) {
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not liked");
        }
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    // ==================== NEW: Notification ====================

    @Transactional
    public List<NotificationDto> getNotifications(Long userId) {
        List<NotificationEntity> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        // Auto mark as read
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return unread.stream().map(this::toNotificationDto).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private void createNotification(Long userId, NotificationType type, Long sourceUserId, Long targetPostId, Long targetCommentId) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setTargetPostId(targetPostId);
        n.setTargetCommentId(targetCommentId);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private void notifyFollowers(Long idolId, PostEntity post) {
        List<FollowEntity> followers = followRepository.findByIdolId(idolId);
        for (FollowEntity f : followers) {
            createNotification(f.getFanId(), NotificationType.NEW_POST,
                    idolId, post.getId(), null);
        }
    }

    private void notifyReply(UserEntity idol, PostEntity post, CommentEntity comment) {
        // Notify the post author if different from idol
        if (!post.getAuthor().getId().equals(idol.getId())) {
            createNotification(post.getAuthor().getId(), NotificationType.REPLY,
                    idol.getId(), post.getId(), comment.getId());
        }
        // Notify other commenters on this post (deduplicated, excluding idol and post author)
        commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).stream()
                .map(c -> c.getAuthor().getId())
                .distinct()
                .filter(uid -> !uid.equals(idol.getId()) && !uid.equals(post.getAuthor().getId()))
                .forEach(uid -> createNotification(uid, NotificationType.REPLY,
                        idol.getId(), post.getId(), comment.getId()));
    }

    private NotificationDto toNotificationDto(NotificationEntity n) {
        String sourceUsername = null;
        if (n.getSourceUserId() != null) {
            sourceUsername = userRepository.findById(n.getSourceUserId())
                    .map(UserEntity::getUsername).orElse(null);
        }
        return new NotificationDto(n.getId(), n.getUserId(), n.getType().name(),
                n.getSourceUserId(), sourceUsername,
                n.getTargetPostId(), n.getTargetCommentId(),
                n.isRead(), n.getCreatedAt());
    }

    // ==================== NEW: News ====================

    @Transactional
    public NewsDto createNews(CreateNewsRequest request) {
        NewsEntity news = new NewsEntity();
        news.setTitle(request.title());
        news.setSummary(request.summary());
        news.setSourceUrl(request.sourceUrl());
        news.setCategory(NewsCategory.valueOf(request.category().toUpperCase()));
        news.setPublishedDate(LocalDate.now());
        news.setCreatedAt(LocalDateTime.now());
        return toNewsDto(newsRepository.save(news));
    }

    @Transactional
    public NewsDto updateNews(Long id, UpdateNewsRequest request) {
        NewsEntity news = newsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "news not found"));
        if (request.title() != null) news.setTitle(request.title());
        if (request.summary() != null) news.setSummary(request.summary());
        if (request.sourceUrl() != null) news.setSourceUrl(request.sourceUrl());
        if (request.category() != null) news.setCategory(NewsCategory.valueOf(request.category().toUpperCase()));
        return toNewsDto(newsRepository.save(news));
    }

    @Transactional
    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "news not found");
        }
        newsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NewsDto> listNews() {
        return newsRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toNewsDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NewsDto> getTodayNews() {
        return newsRepository.findByPublishedDateOrderByCreatedAtDesc(LocalDate.now()).stream()
                .map(this::toNewsDto).toList();
    }

    @Transactional(readOnly = true)
    public NewsDto getNewsById(Long id) {
        return toNewsDto(newsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "news not found")));
    }

    private NewsDto toNewsDto(NewsEntity news) {
        return new NewsDto(news.getId(), news.getTitle(), news.getSummary(),
                news.getSourceUrl(), news.getCategory().name(),
                news.getPublishedDate(), news.getCreatedAt());
    }

    // ==================== Existing: Admin ====================

    @Transactional(readOnly = true)
    public AdminDashboardDto adminDashboard() {
        List<AdminUserDto> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(user -> new AdminUserDto(user.getId(), user.getUsername(),
                        user.getRole().name(), user.getAvatarUrl(), user.getPersonaSummary()))
                .toList();
        List<AdminIdolStatusDto> statuses = idolStatusRepository.findAll().stream()
                .sorted(Comparator.comparing(IdolStatusEntity::getIdolId))
                .map(status -> new AdminIdolStatusDto(status.getIdolId(), status.getStamina(),
                        status.getMood(), status.getLastActiveAt()))
                .toList();
        List<PostDto> posts = postRepository.findAll().stream()
                .sorted(Comparator.comparing(PostEntity::getCreatedAt).reversed())
                .map(DtoMapper::toPostDto).toList();
        List<CommentDto> comments = commentRepository.findAll().stream()
                .sorted(Comparator.comparing(CommentEntity::getCreatedAt).reversed())
                .map(DtoMapper::toCommentDto).toList();
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
        return Map.of("deletedUsers", users, "deletedStatuses", statuses,
                "deletedPosts", posts, "deletedComments", comments);
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
        if (request.username() != null && !request.username().isBlank()) idol.setUsername(request.username().trim());
        if (request.avatarUrl() != null) idol.setAvatarUrl(request.avatarUrl().trim());
        if (request.personaSummary() != null) idol.setPersonaSummary(request.personaSummary().trim());
        if (request.stamina() != null) status.setStamina(clamp(request.stamina()));
        if (request.mood() != null) status.setMood(clamp(request.mood()));
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
        if (request.avatarUrl() != null) fan.setAvatarUrl(request.avatarUrl().trim());
        if (request.personaSummary() != null) fan.setPersonaSummary(request.personaSummary().trim());
        userRepository.save(fan);
        return new FanProfileDto(fan.getId(), fan.getUsername(), fan.getAvatarUrl(), fan.getPersonaSummary());
    }

    // ==================== Helpers ====================

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
        return Math.max(0, Math.min(10000, value));
    }
}
