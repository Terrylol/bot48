package com.cyber48.backend.controller;

import com.cyber48.backend.dto.CommentDto;
import com.cyber48.backend.dto.FeedDto;
import com.cyber48.backend.dto.IdolSummaryDto;
import com.cyber48.backend.dto.NewsDto;
import com.cyber48.backend.dto.PaginatedFeedDto;
import com.cyber48.backend.service.Cyber48Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicController {
    private final Cyber48Service cyber48Service;

    public PublicController(Cyber48Service cyber48Service) {
        this.cyber48Service = cyber48Service;
    }

    @GetMapping("/idols")
    public List<IdolSummaryDto> listIdols() {
        return cyber48Service.listIdols();
    }

    @GetMapping("/idols/{idolId}/feed")
    public FeedDto feed(@PathVariable Long idolId) {
        return cyber48Service.getFeed(idolId);
    }

    @GetMapping("/idols/{idolId}/feed/paged")
    public PaginatedFeedDto paginatedFeed(
            @PathVariable Long idolId,
            @RequestParam(defaultValue = "1") int idolPage,
            @RequestParam(defaultValue = "10") int idolPageSize,
            @RequestParam(defaultValue = "1") int fanPage,
            @RequestParam(defaultValue = "10") int fanPageSize
    ) {
        return cyber48Service.getPaginatedFeed(idolId, idolPage, idolPageSize, fanPage, fanPageSize);
    }

    @GetMapping("/idols/{idolId}/followers/count")
    public Map<String, Long> followerCount(@PathVariable Long idolId) {
        return Map.of("count", cyber48Service.getFollowerCount(idolId));
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> comments(@PathVariable Long postId) {
        return cyber48Service.getComments(postId);
    }

    @GetMapping("/posts/{postId}/likes/count")
    public Map<String, Long> likeCount(@PathVariable Long postId) {
        return Map.of("count", cyber48Service.getLikeCount(postId));
    }

    @GetMapping("/news/today")
    public List<NewsDto> todayNews() {
        return cyber48Service.getTodayNews();
    }
}
