package com.cmc.board;

import com.cmc.board.comment.*;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.Post;
import com.cmc.board.post.PostRepository;
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
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;

    @InjectMocks private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private final String email = "user@test.com";

    @BeforeEach
    void setUp() {
        // 정적 팩토리 메서드로 유저 생성
        testUser = User.create(email, "password123", "댓글작성자");

        testPost = new Post();
        ReflectionTestUtils.setField(testPost, "postId", 1L);

        testComment = new Comment();
        ReflectionTestUtils.setField(testComment, "commentId", 10L);
        testComment.setUser(testUser);
        testComment.setContent("기본 댓글");
    }

    @Nested
    @DisplayName("댓글 생성 (createComment)")
    class CreateComment {

        @Test
        @DisplayName("성공: 게시글에 직접 댓글을 작성한다.")
        void createComment_Success_PostOnly() {
            // given
            CommentRequest request = new CommentRequest("댓글내용", 1L, null);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            // when
            CommentResponse response = commentService.createComment(request, email);

            // then
            assertThat(response.getContent()).isEqualTo("댓글내용");
            verify(commentRepository, times(1)).save(any(Comment.class));
            verify(commentRepository, never()).findById(anyLong()); // 부모댓글 조회는 안 일어나야 함
        }

        @Test
        @DisplayName("성공: 특정 댓글에 대댓글을 작성한다.")
        void createComment_Success_WithParent() {
            // given
            CommentRequest request = new CommentRequest("대댓글내용", null, 10L);
            Comment parentComment = new Comment();
            ReflectionTestUtils.setField(parentComment, "commentId", 10L);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(10L)).willReturn(Optional.of(parentComment));

            // when
            commentService.createComment(request, email);

            // then
            verify(commentRepository, times(1)).save(any(Comment.class));
            verify(postRepository, never()).findById(anyLong()); // 게시글 조지는 안 일어나야 함
        }

        @Test
        @DisplayName("실패: 유저를 찾을 수 없는 경우")
        void createComment_Fail_UserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class,
                    () -> commentService.createComment(new CommentRequest("내용", 1L, null), email));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글에 댓글을 다는 경우")
        void createComment_Fail_PostNotFound() {
            CommentRequest request = new CommentRequest("내용", 99L, null);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(postRepository.findById(99L)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> commentService.createComment(request, email));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 부모 댓글에 대댓글을 다는 경우")
        void createComment_Fail_ParentNotFound() {
            CommentRequest request = new CommentRequest("내용", null, 99L);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(99L)).willReturn(Optional.empty());

            assertThrows(BusinessLogicException.class, () -> commentService.createComment(request, email));
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class FindComments {

        @Test
        @DisplayName("성공: 게시글의 댓글 목록을 최신순으로 조회한다.")
        void findPostComments_Success() {
            given(commentRepository.findByPostPostIdOrderByCommentIdDesc(1L))
                    .willReturn(List.of(testComment));

            List<CommentResponse> result = commentService.findPostComments(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).isEqualTo("기본 댓글");
        }

        @Test
        @DisplayName("성공: 부모 댓글의 대댓글 목록을 조회한다.")
        void findChildComments_Success() {
            given(commentRepository.findByParentCommentIdOrderByCommentIdDesc(10L))
                    .willReturn(List.of(testComment));

            List<CommentResponse> result = commentService.findChildComments(10L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("댓글 수정 (updateComment)")
    class UpdateComment {

        @Test
        @DisplayName("성공: 본인이 작성한 댓글을 수정한다.")
        void updateComment_Success() {
            CommentRequest request = new CommentRequest("수정된 내용", null, null);
            given(commentRepository.findById(10L)).willReturn(Optional.of(testComment));

            CommentResponse response = commentService.updateComment(10L, request, email);

            assertThat(testComment.getContent()).isEqualTo("수정된 내용");
            verify(commentRepository, times(1)).save(testComment);
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 유저가 수정을 시도할 때")
        void updateComment_Fail_NotAuthorized() {
            User otherUser = User.create("other@test.com", "pass", "타인");
            ReflectionTestUtils.setField(testComment, "user", otherUser);
            given(commentRepository.findById(10L)).willReturn(Optional.of(testComment));

            assertThrows(BusinessLogicException.class,
                    () -> commentService.updateComment(10L, new CommentRequest("수정", null, null), email));
        }
    }

    @Nested
    @DisplayName("댓글 삭제 (removeComment)")
    class RemoveComment {

        @Test
        @DisplayName("성공: 본인이 작성한 댓글을 삭제한다.")
        void removeComment_Success() {
            // [Given]
            given(commentRepository.findById(10L)).willReturn(Optional.of(testComment));

            // [When]
            commentService.removeComment(10L, email);

            // [Then]
            verify(commentRepository, times(1)).delete(testComment);
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 유저가 삭제 시도 시 NOT_AUTHORIZED 예외가 발생한다.")
        void removeComment_Fail_NotAuthorized() {
            // [Given] 작성자가 다른 유저인 댓글 생성
            User otherUser = User.create("other@test.com", "password", "타인");

            // 기존 testComment의 작성자를 타인으로 교체 (리플렉션 사용)
            ReflectionTestUtils.setField(testComment, "user", otherUser);

            given(commentRepository.findById(10L)).willReturn(Optional.of(testComment));

            // [When & Then] 94번 라인을 실행시키는 핵심 테스트
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> commentService.removeComment(10L, email));

            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.NOT_AUTHORIZED);
            verify(commentRepository, never()).delete(any(Comment.class)); // 실제 삭제는 일어나지 않아야 함
        }

        @Test
        @DisplayName("실패: 삭제할 댓글이 존재하지 않을 때")
        void removeComment_Fail_NotFound() {
            given(commentRepository.findById(10L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> commentService.removeComment(10L, email));
        }
    }
}