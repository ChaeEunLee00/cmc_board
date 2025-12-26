package com.cmc.board.comment.dto;

import com.cmc.board.comment.domain.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String user;
    private Long postId;
    private Long parentId;
    private int childCount;

    public CommentResponse(Comment comment){
        this.commentId = comment.getCommentId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.user = comment.getUser().getNickname();
        if(comment.getPost() != null) this.postId = comment.getPost().getPostId();
        if(comment.getParent() != null) this.parentId = comment.getParent().getCommentId();
        this.childCount = comment.getChildren().size();
    }
}
