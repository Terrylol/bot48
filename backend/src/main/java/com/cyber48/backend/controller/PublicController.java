package com.cyber48.backend.controller;

import com.cyber48.backend.dto.CommentDto;
import com.cyber48.backend.dto.FeedDto;
import com.cyber48.backend.dto.IdolSummaryDto;
import com.cyber48.backend.service.Cyber48Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> comments(@PathVariable Long postId) {
        return cyber48Service.getComments(postId);
    }
}
