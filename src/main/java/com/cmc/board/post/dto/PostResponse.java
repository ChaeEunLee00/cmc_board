package com.cmc.board.post.dto;

import com.cmc.board.post.domain.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class PostResponse {

    private Long postId;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String user;

    private String category;

    public PostResponse(Post post){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.user = post.getUser().getNickname();
        this.category = post.getCategory().getName();
    }
}
