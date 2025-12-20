package com.cmc.board;

import com.cmc.board.bookmark.Bookmark;
import com.cmc.board.bookmark.BookmarkRepository;
import com.cmc.board.bookmark.BookmarkService;
import com.cmc.board.category.Category;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.Post;
import com.cmc.board.post.PostRepository;
import com.cmc.board.post.PostResponse;
import com.cmc.board.user.User;
import com.cmc.board.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * [Narrative]
 * 사용자는 마음에 드는 게시글을 북마크에 저장하고, 저장된 목록을 확인할 수 있어야 한다.
 * 북마크 목록은 최근에 추가한 순서대로 정렬되어야 하며, 필요 시 삭제가 가능해야 한다.
 */
@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookmarkService bookmarkService;

    @Nested
    @DisplayName("북마크 생성 테스트")
    class CreateBookmarkTest {
        @Test
        @DisplayName("성공: 유효한 게시글과 유저로 북마크를 생성한다.")
        void createBookmark_Success() {
            // [Given]
            Long postId = 1L;
            String email = "test@test.com";
            User user = new User(); user.setEmail(email);
            Post post = new Post(); post.setPostId(postId);

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // [When]
            bookmarkService.createBookmark(postId, email);

            // [Then]
            ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
            verify(bookmarkRepository, times(1)).save(bookmarkCaptor.capture());

            Bookmark savedBookmark = bookmarkCaptor.getValue();
            assertThat(savedBookmark.getUser()).isEqualTo(user);
            assertThat(savedBookmark.getPost()).isEqualTo(post);
        }
    }

    @Nested
    @DisplayName("북마크 목록 조회 테스트")
    class FindBookmarksTest {
        @Test
        @DisplayName("성공: 유저의 북마크 리스트를 PostResponse 형태로 반환한다.")
        void findBookmarks_Success() {
            // [Given]
            String email = "test@test.com";
            User user = new User(); user.setEmail(email);

            // PostResponse 생성을 위해 연관 객체(User, Category) 세팅 필수
            User author = new User(); author.setNickname("작성자");
            Category category = new Category(); category.setName("카테고리");

            Post post = new Post();
            post.setTitle("북마크한 글");
            post.setUser(author);
            post.setCategory(category);

            Bookmark bookmark = new Bookmark();
            bookmark.setPost(post);
            bookmark.setUser(user);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(bookmarkRepository.findByUserOrderByBookmarkIdDesc(user)).willReturn(List.of(bookmark));

            // [When]
            List<PostResponse> result = bookmarkService.findBookmarks(email);

            // [Then]
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("북마크한 글");
            assertThat(result.get(0).getUser()).isEqualTo("작성자");
        }
    }

    @Nested
    @DisplayName("북마크 삭제 테스트")
    class RemoveBookmarkTest {
        @Test
        @DisplayName("성공: 유저와 게시글 정보가 일치하는 북마크를 삭제한다.")
        void removeBookmark_Success() {
            // [Given]
            Long postId = 1L;
            String email = "test@test.com";
            User user = new User(); user.setEmail(email);
            Post post = new Post(); post.setPostId(postId);
            Bookmark bookmark = new Bookmark();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(bookmarkRepository.findByUserAndPost(user, post)).willReturn(Optional.of(bookmark));

            // [When]
            bookmarkService.removeBookmark(postId, email);

            // [Then]
            verify(bookmarkRepository, times(1)).delete(bookmark);
        }

        @Test
        @DisplayName("실패: 해당 북마크가 존재하지 않으면 예외가 발생한다.")
        void removeBookmark_Fail_NotFound() {
            // [Given]
            Long postId = 1L;
            String email = "test@test.com";
            User user = new User();
            Post post = new Post();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(bookmarkRepository.findByUserAndPost(user, post)).willReturn(Optional.empty());

            // [When & Then]
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> bookmarkService.removeBookmark(postId, email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.BOOKMARK_NOT_FOUND);
        }
    }
}