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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        testUser = new User();
        testUser.setEmail(email);
        testUser.setNickname("작성자");

        testPost = new Post();
        testPost.setPostId(1L);

        testComment = new Comment();
        testComment.setCommentId(10L);
        testComment.setUser(testUser);
        testComment.setContent("기본 댓글");
    }

    @Nested
    @DisplayName("댓글 생성 (createComment)")
    class CreateComment {

        @Test
        @DisplayName("성공: 게시글 댓글 작성 (parentId가 null일 때)")
        void createComment_Success_PostOnly() {
            // [Given]
            CommentRequest request = new CommentRequest("내용", 1L, null);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));

            // [When]
            CommentResponse response = commentService.createComment(request, email);

            // [Then]
            assertThat(response.getContent()).isEqualTo("내용");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("성공: 대댓글 작성 (parentId가 존재할 때)")
        void createComment_Success_WithParent() {
            // [Given] 부모 댓글이 존재하는 시나리오
            CommentRequest request = new CommentRequest("대댓글", null, 10L);
            Comment parentComment = new Comment();
            parentComment.setCommentId(10L);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(10L)).willReturn(Optional.of(parentComment));

            // [When]
            commentService.createComment(request, email);

            // [Then]
            verify(commentRepository, times(1)).save(any(Comment.class));
            // parentId 분기가 실행되었는지 간접 확인 가능
        }

        @Test
        @DisplayName("실패: 유저를 찾을 수 없는 경우")
        void createComment_Fail_UserNotFound() {
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> commentService.createComment(new CommentRequest("내용", 1L, null), email));
        }

        @Test
        @DisplayName("실패: 부모 댓글(parentId)을 찾을 수 없는 경우")
        void createComment_Fail_CommentNotFound() {
            CommentRequest request = new CommentRequest("대댓글", null, 99L);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(99L)).willReturn(Optional.empty());

            BusinessLogicException ex = assertThrows(BusinessLogicException.class, () -> commentService.createComment(request, email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.COMMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 조회 (findPostComments & findChildComments)")
    class FindComments {

        @Test
        @DisplayName("성공: 게시글의 댓글 목록 조회")
        void findPostComments_Success() {
            // [Given]
            given(commentRepository.findByPostPostIdOrderByCommentIdDesc(1L))
                    .willReturn(List.of(testComment));

            // [When]
            List<CommentResponse> responses = commentService.findPostComments(1L);

            // [Then]
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getContent()).isEqualTo("기본 댓글");
        }

        @Test
        @DisplayName("성공: 대댓글 목록 조회")
        void findChildComments_Success() {
            // [Given]
            given(commentRepository.findByParentCommentIdOrderByCommentIdDesc(10L))
                    .willReturn(List.of(testComment));

            // [When]
            List<CommentResponse> responses = commentService.findChildComments(10L);

            // [Then]
            assertThat(responses).hasSize(1);
        }
    }

    @Nested
    @DisplayName("댓글 수정 (updateComment)")
    class UpdateComment {

        @Test
        @DisplayName("실패: 댓글을 찾을 수 없는 경우")
        void updateComment_Fail_NotFound() {
            given(commentRepository.findById(anyLong())).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> commentService.updateComment(1L, new CommentRequest("수정", null, null), email));
        }

        @Test
        @DisplayName("실패: 본인이 작성하지 않은 댓글 수정 시도")
        void updateComment_Fail_NotAuthorized() {
            // [Given] 작성자가 다른 댓글
            User otherUser = new User();
            otherUser.setEmail("other@test.com");
            testComment.setUser(otherUser);

            given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));

            // [When & Then]
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> commentService.updateComment(1L, new CommentRequest("수정", null, null), email));
            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.NOT_AUTHORIZED);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 (removeComment)")
    class RemoveComment {

        @Test
        @DisplayName("실패: 삭제 권한 없음")
        void removeComment_Fail_NotAuthorized() {
            User otherUser = new User();
            otherUser.setEmail("other@test.com");
            testComment.setUser(otherUser);

            given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));

            assertThrows(BusinessLogicException.class, () -> commentService.removeComment(1L, email));
        }

        @Test
        @DisplayName("실패: 삭제할 댓글이 없음")
        void removeComment_Fail_NotFound() {
            given(commentRepository.findById(1L)).willReturn(Optional.empty());
            assertThrows(BusinessLogicException.class, () -> commentService.removeComment(1L, email));
        }
    }
}