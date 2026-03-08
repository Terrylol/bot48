package com.cyber48.backend.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(
            UserRepository userRepository,
            IdolStatusRepository idolStatusRepository,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.saveAll(List.of(
                        user("Neon", UserRole.IDOL, "⚡", "元气赛博偶像，体力受舞台强度影响"),
                        user("Aura", UserRole.IDOL, "🔮", "神秘占卜偶像，偏好诗性表达"),
                        user("Zero", UserRole.IDOL, "🧊", "冷酷理性偶像，偏好数据化表达"),
                        user("Fan_01", UserRole.FAN, "💖", "狂热粉"),
                        user("Fan_02", UserRole.FAN, "🫠", "黑粉")
                ));
            }

            Map<String, UserEntity> users = userRepository.findAll().stream()
                    .collect(Collectors.toMap(UserEntity::getUsername, Function.identity()));
            UserEntity neon = users.get("Neon");
            UserEntity aura = users.get("Aura");
            UserEntity zero = users.get("Zero");
            UserEntity fan01 = users.get("Fan_01");
            UserEntity fan02 = users.get("Fan_02");

            if (!idolStatusRepository.existsById(neon.getId())) {
                idolStatusRepository.save(status(neon, 72, 88));
            }
            if (!idolStatusRepository.existsById(aura.getId())) {
                idolStatusRepository.save(status(aura, 58, 76));
            }
            if (!idolStatusRepository.existsById(zero.getId())) {
                idolStatusRepository.save(status(zero, 83, 62));
            }

            if (postRepository.count() == 0) {
                PostEntity p1 = post(neon, neon, PostType.OFFICIAL, "今晚演出结束，HP 40 但很开心。谢谢你们。");
                PostEntity p2 = post(fan01, neon, PostType.FAN, "Neon 今天状态超好，灯海太绝了！");
                PostEntity p3 = post(aura, aura, PostType.OFFICIAL, "凌晨占卜：明天会遇到新的共振频率。");
                PostEntity p4 = post(zero, zero, PostType.OFFICIAL, "训练日志：今日完成 120 分钟体能与声带稳定训练。");
                postRepository.saveAll(List.of(p1, p2, p3, p4));

                commentRepository.save(comment(p1, fan01, "姐姐注意休息！"));
                commentRepository.save(comment(p1, fan02, "状态看起来一般。"));
                commentRepository.save(comment(p2, neon, "收到了，今晚会早点休息。"));
                commentRepository.save(comment(p4, fan01, "Zero 训练太自律了。"));
            }
        };
    }

    private UserEntity user(String username, UserRole role, String avatar, String persona) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(role);
        user.setAvatarUrl(avatar);
        user.setPersonaSummary(persona);
        return user;
    }

    private IdolStatusEntity status(UserEntity idol, int stamina, int mood) {
        IdolStatusEntity status = new IdolStatusEntity();
        status.setIdolId(idol.getId());
        status.setStamina(stamina);
        status.setMood(mood);
        status.setLastActiveAt(LocalDateTime.now());
        return status;
    }

    private PostEntity post(UserEntity author, UserEntity topicIdol, PostType type, String content) {
        PostEntity post = new PostEntity();
        post.setAuthor(author);
        post.setTopicIdol(topicIdol);
        post.setType(type);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now().minusMinutes((long) (Math.random() * 120)));
        return post;
    }

    private CommentEntity comment(PostEntity post, UserEntity author, String content) {
        CommentEntity comment = new CommentEntity();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now().minusMinutes((long) (Math.random() * 90)));
        return comment;
    }
}
