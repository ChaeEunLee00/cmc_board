package com.cmc.board;

import com.cmc.board.category.Category;
import com.cmc.board.category.CategoryRepository;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.*;
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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private PostService postService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private final String email = "test@test.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(email);
        testUser.setNickname("작성자");

        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setName("자유게시판");

        testPost = new Post();
        testPost.setPostId(1L);
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTitle("기존 제목");
        testPost.setContent("기존 내용");
    }

    @Nested
    @DisplayName("게시글 생성 (createPost)")
    class CreatePost {
        @Test
        @DisplayName("성공: 정상적인 포스트 생성")
        void createPost_Success() {
            PostRequest request = new PostRequest("제목", "내용", 1L);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

            PostResponse response = postService.createPost(request, email);

            assertThat(response.getTitle()).isEqualTo("제목");
            verify(postRepository, times(1)).save(any(Post.class));
        }

        @Test
        @DisplayName("실패: 제목/내용/카테고리 중 하나라도 null인 경우")
        void createPost_Fail_InputNull() {
            PostRequest request = new PostRequest(null, "내용", 1L); // 제목 null

            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> postService.createPost(request, email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.INPUT_CANNOT_BE_NULL);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 유저 이메일인 경우")
        void createPost_Fail_UserNotFound() {
            PostRequest request = new PostRequest("제목", "내용", 1L);
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> postService.createPost(request, email));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리 ID인 경우")
        void createPost_Fail_CategoryNotFound() {
            PostRequest request = new PostRequest("제목", "내용", 1L);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(categoryRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> postService.createPost(request, email));
        }
    }

    @Nested
    @DisplayName("게시글 조회 (findPost & findPosts)")
    class FindPost {
        @Test
        @DisplayName("단일 조회 성공")
        void findPost_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            PostResponse response = postService.findPost(1L);
            assertThat(response.getTitle()).isEqualTo("기존 제목");
        }

        @Test
        @DisplayName("단일 조회 실패: 존재하지 않는 게시글")
        void findPost_Fail_NotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> postService.findPost(1L));
        }

        @Test
        @DisplayName("목록 조회 성공 (페이징)")
        void findPosts_Success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("postId").descending());
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(testPost)));

            List<PostResponse> result = postService.findPosts(0, 10);

            assertThat(result).hasSize(1);
            verify(postRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("게시글 수정 (updatePost)")
    class UpdatePost {
        @Test
        @DisplayName("성공: 제목, 내용, 카테고리를 모두 수정")
        void updatePost_Success_AllFields() {
            PostRequest request = new PostRequest("새제목", "새내용", 2L);
            Category newCategory = new Category(); newCategory.setName("새카테고리");

            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(categoryRepository.findById(2L)).willReturn(Optional.of(newCategory));

            postService.updatePost(1L, request, email);

            assertThat(testPost.getTitle()).isEqualTo("새제목");
            assertThat(testPost.getContent()).isEqualTo("새내용");
            assertThat(testPost.getCategory()).isEqualTo(newCategory);
        }

        @Test
        @DisplayName("성공: 수정할 데이터가 없는 경우(null) 기존 데이터 유지")
        void updatePost_Success_NoFields() {
            PostRequest request = new PostRequest(null, null, null);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            postService.updatePost(1L, request, email);

            assertThat(testPost.getTitle()).isEqualTo("기존 제목");
            assertThat(testPost.getContent()).isEqualTo("기존 내용");
        }

        @Test
        @DisplayName("실패: 수정 시 카테고리를 찾을 수 없는 경우")
        void updatePost_Fail_CategoryNotFound() {
            PostRequest request = new PostRequest(null, null, 99L);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> postService.updatePost(1L, request, email));
        }

        @Test
        @DisplayName("실패: 본인이 아닌 유저가 수정을 시도할 경우")
        void updatePost_Fail_NotAuthorized() {
            PostRequest request = new PostRequest("수정", "수정", null);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> postService.updatePost(1L, request, "other@test.com"));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.NOT_AUTHORIZED);
        }
    }

    @Nested
    @DisplayName("게시글 삭제 (removePost)")
    class RemovePost {
        @Test
        @DisplayName("성공: 작성자가 삭제")
        void removePost_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            postService.removePost(1L, email);
            verify(postRepository, times(1)).delete(testPost);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글 삭제 시도")
        void removePost_Fail_NotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> postService.removePost(1L, email));
        }
    }
}