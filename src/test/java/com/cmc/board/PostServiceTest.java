package com.cmc.board;

import com.cmc.board.category.domain.Category;
import com.cmc.board.category.repository.CategoryRepository;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.domain.Post;
import com.cmc.board.post.dto.PostRequest;
import com.cmc.board.post.dto.PostResponse;
import com.cmc.board.post.repository.PostRepository;
import com.cmc.board.post.service.PostService;
import com.cmc.board.user.domain.User;
import com.cmc.board.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

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
        testUser = User.create(email, "password123", "작성자");

        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);
        testCategory.setName("자유게시판");

        testPost = new Post();
        ReflectionTestUtils.setField(testPost, "postId", 1L);
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTitle("기존 제목");
        testPost.setContent("기존 내용");
    }

    @Nested
    @DisplayName("게시글 생성 (createPost)")
    class CreatePost {
        @Test
        @DisplayName("성공: 정상 저장 (모든 라인 통과)")
        void createPost_Success() {
            PostRequest request = new PostRequest("제목", "내용", 1L);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

            PostResponse response = postService.createPost(request, email);

            assertThat(response.getTitle()).isEqualTo("제목");
            verify(postRepository, times(1)).save(any(Post.class));
        }

        @Test
        @DisplayName("실패: 입력값 누락 (29번 라인 노란색 해결)")
        void createPost_Fail_InputNull() {
            // 제목, 내용, 카테고리 중 하나라도 null인 경우
            PostRequest request = new PostRequest(null, "내용", 1L);

            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> postService.createPost(request, email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.INPUT_CANNOT_BE_NULL);
        }

        @Test
        @DisplayName("실패: 유저 없음 (34번 라인 해결)")
        void createPost_Fail_UserNotFound() {
            PostRequest request = new PostRequest("제목", "내용", 1L);
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> postService.createPost(request, email));
        }

        @Test
        @DisplayName("실패: 카테고리 없음 (39번 라인 해결)")
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
        @DisplayName("단건 조회 성공: 정상 리턴 (58번 라인 빨간색 해결)")
        void findPost_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            PostResponse response = postService.findPost(1L);

            assertThat(response.getPostId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("단건 조회 실패: 게시글 미존재 (56번 라인 해결)")
        void findPost_Fail_NotFound() {
            given(postRepository.findById(1L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> postService.findPost(1L));
        }

        @Test
        @DisplayName("목록 조회 성공: 페이징 데이터 반환")
        void findPosts_Success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("postId").descending());
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(testPost)));

            List<PostResponse> result = postService.findPosts(0, 10);
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("게시글 수정 (updatePost)")
    class UpdatePost {
        @Test
        @DisplayName("성공: 작성자 확인 및 모든 필드 수정")
        void updatePost_Success() {
            PostRequest request = new PostRequest("수정제목", "수정내용", 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

            postService.updatePost(1L, request, email);

            assertThat(testPost.getTitle()).isEqualTo("수정제목");
            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("실패: 작성자 불일치 (78번 라인 빨간색 해결)")
        void updatePost_Fail_Auth() {
            PostRequest request = new PostRequest("해킹", "해킹", null);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            // 다른 이메일 주소 전달하여 equals(email)이 false가 되도록 유도
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> postService.updatePost(1L, request, "hacker@test.com"));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.NOT_AUTHORIZED);
        }

        @Test
        @DisplayName("분기: 수정 필드(제목, 내용)가 null일 때 (81, 82번 라인 노란색 해결)")
        void updatePost_PartialNull() {
            PostRequest request = new PostRequest(null, null, null);
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            postService.updatePost(1L, request, email);

            assertThat(testPost.getTitle()).isEqualTo("기존 제목"); // 변경되지 않음
            verify(categoryRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("분기: 카테고리만 수정할 때 (83번 라인 노란색 해결)")
        void updatePost_CategoryOnly() {
            PostRequest request = new PostRequest(null, null, 2L);
            Category newCategory = new Category();
            ReflectionTestUtils.setField(newCategory, "categoryId", 2L);

            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(categoryRepository.findById(2L)).willReturn(Optional.of(newCategory));

            postService.updatePost(1L, request, email);

            assertThat(testPost.getCategory().getCategoryId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("게시글 삭제 (removePost)")
    class RemovePost {
        @Test
        @DisplayName("성공: 본인 작성글 삭제")
        void removePost_Success() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            postService.removePost(1L, email);

            verify(postRepository, times(1)).delete(testPost);
        }

        @Test
        @DisplayName("실패: 작성자 불일치 (103번 라인 빨간색 해결)")
        void removePost_Fail_Auth() {
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> postService.removePost(1L, "other@test.com"));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.NOT_AUTHORIZED);
        }
    }
}