package com.cmc.board.post;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostRequest {
    private String title;

    private String content;

    private Long category;
}
