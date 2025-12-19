package com.cmc.board.post;

import com.cmc.board.bookmark.Bookmark;
import com.cmc.board.category.Category;
import com.cmc.board.comment.Comment;
import com.cmc.board.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE) // 게시글 삭제 시 댓글도 삭제
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE) // 게시글 삭제 시 북마크도 삭제
    private List<Bookmark> bookmarks = new ArrayList<>();
}