package com.cmc.board.bookmark.repository;

import com.cmc.board.bookmark.domain.Bookmark;
import com.cmc.board.post.domain.Post;
import com.cmc.board.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserOrderByBookmarkIdDesc(User user);
    Optional<Bookmark> findByUserAndPost(User user, Post post);
    boolean existsByPost_PostIdAndUser_Email(Long postId, String email);

}
