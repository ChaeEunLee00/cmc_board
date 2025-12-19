package com.cmc.board.bookmark;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.Post;
import com.cmc.board.post.PostRepository;
import com.cmc.board.post.PostResponse;
import com.cmc.board.user.User;
import com.cmc.board.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void createBookmark(Long postId, String email){
        // 게시글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        // 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        Bookmark bookmark = new Bookmark();
        bookmark.setPost(post);
        bookmark.setUser(user);

        bookmarkRepository.save(bookmark);
    }

    public List<PostResponse> findBookmarks(String email){
        // 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        // 유저의 북마크 찾기
        List<Bookmark> bookmarks = bookmarkRepository.findByUserOrderByBookmarkIdDesc(user);

        return bookmarks.stream()
                .map(bookmark -> new PostResponse(bookmark.getPost()))
                .collect(Collectors.toList());
    }

    public void removeBookmark(Long postId, String email){
        // 게시글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        // 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        // 해당 유저의 해당 게시글에 대한 북마크 찾기
        Bookmark bookmark = bookmarkRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }
}
