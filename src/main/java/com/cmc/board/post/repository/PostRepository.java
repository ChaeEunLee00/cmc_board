package com.cmc.board.post.repository;

import com.cmc.board.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByCategoryCategoryId(Long categoryId);
}
