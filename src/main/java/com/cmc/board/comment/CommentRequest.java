package com.cmc.board.comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CommentRequest {

    @NotNull
    private String content;

    private Long postId;

    private Long parentId;

}
