package com.cmc.board.bookmark;

import com.cmc.board.post.Post;
import com.cmc.board.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserOrderByBookmarkIdDesc(User user);
    Optional<Bookmark> findByUserAndPost(User user, Post post);
}
