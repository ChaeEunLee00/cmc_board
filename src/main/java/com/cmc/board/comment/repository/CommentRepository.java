package com.cmc.board.comment.repository;

import com.cmc.board.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostPostIdOrderByCommentIdDesc(Long postId);
    List<Comment> findByParentCommentIdOrderByCommentIdDesc(Long parentId);
}
