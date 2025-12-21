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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookmarkService bookmarkService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private Bookmark testBookmark;
    private final String email = "test@test.com";

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        testUser = User.create(email, "password", "유저닉네임");

        // 2. 카테고리 생성 (NPE 해결 핵심!)
        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);
        testCategory.setName("자유게시판");

        // 3. 게시글 생성 및 연관관계 설정
        testPost = new Post();
        ReflectionTestUtils.setField(testPost, "postId", 1L);
        testPost.setTitle("테스트 게시글");
        testPost.setContent("테스트 내용");
        testPost.setUser(testUser);       // PostResponse 생성자에서 사용됨
        testPost.setCategory(testCategory); // PostResponse 생성자(Line 37)에서 사용됨

        // 4. 북마크 생성
        testBookmark = new Bookmark();
        ReflectionTestUtils.setField(testBookmark, "bookmarkId", 100L);
        testBookmark.setUser(testUser);
        testBookmark.setPost(testPost);
    }

    @Nested
    @DisplayName("북마크 생성 (createBookmark)")
    class CreateBookmark {
        @Test
        @DisplayName("성공: 게시글과 유저가 존재하면 북마크가 저장된다.")
        void createBookmark_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));

            bookmarkService.createBookmark(1L, email);

            verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("실패: 게시글이 없으면 POST_NOT_FOUND")
        void createBookmark_Fail_PostNotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> bookmarkService.createBookmark(1L, email));
        }

        @Test
        @DisplayName("실패: 유저가 없으면 USER_NOT_FOUND")
        void createBookmark_Fail_UserNotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> bookmarkService.createBookmark(1L, email));
        }
    }

    @Nested
    @DisplayName("북마크 조회 (findBookmarks)")
    class FindBookmarks {
        @Test
        @DisplayName("성공: 유저의 북마크 목록을 반환한다. (NPE 해결 확인)")
        void findBookmarks_Success() {
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(bookmarkRepository.findByUserOrderByBookmarkIdDesc(testUser)).willReturn(List.of(testBookmark));

            List<PostResponse> result = bookmarkService.findBookmarks(email);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("자유게시판");
        }

        @Test
        @DisplayName("실패: 유저가 없으면 예외 발생")
        void findBookmarks_Fail_UserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> bookmarkService.findBookmarks(email));
        }
    }

    @Nested
    @DisplayName("북마크 삭제 (removeBookmark)")
    class RemoveBookmark {
        @Test
        @DisplayName("성공: 북마크를 찾아 삭제한다.")
        void removeBookmark_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(bookmarkRepository.findByUserAndPost(testUser, testPost)).willReturn(Optional.of(testBookmark));

            bookmarkService.removeBookmark(1L, email);

            verify(bookmarkRepository, times(1)).delete(testBookmark);
        }

        @Test
        @DisplayName("실패: 북마크를 찾을 수 없으면 BOOKMARK_NOT_FOUND")
        void removeBookmark_Fail_NotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(bookmarkRepository.findByUserAndPost(testUser, testPost)).willReturn(Optional.empty());

            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> bookmarkService.removeBookmark(1L, email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.BOOKMARK_NOT_FOUND);
        }
    }
}