package com.cmc.board.post;

import com.cmc.board.bookmark.Bookmark;
import com.cmc.board.category.Category;
import com.cmc.board.comment.Comment;
import com.cmc.board.user.User;
import jakarta.persistence.*;
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
}
