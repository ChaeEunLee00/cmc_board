package com.cmc.board.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentRequest {

    @NotNull
    private String content;

    private Long postId;

    private Long parentId;

}
