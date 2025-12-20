package com.cmc.board.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PostRequest {
    private String title;

    private String content;

    private Long category;
}
