package com.cmc.board.comment;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.Post;
import com.cmc.board.post.PostRepository;
import com.cmc.board.user.User;
import com.cmc.board.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentResponse createComment(CommentRequest request, String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        if(request.getPostId() != null){
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));
            comment.setPost(post);
        }
        if(request.getParentId() != null){
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }

        commentRepository.save(comment);

        // 입력값 생성
        return new CommentResponse(comment);
    }

    public List<CommentResponse> findPostComments(Long postId) {
        // 쿼리 메서드를 사용하거나 전체 조회 후 필터링합니다.
        List<Comment> parentComments = commentRepository.findByPostPostIdOrderByCommentIdDesc(postId);

        // DTO로 변환하여 반환 (생성자에서 자식 개수가 계산됨)
        return parentComments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> findChildComments(Long parentId) {
        // 1. 해당 parentId를 부모로 가진 대댓글 리스트 조회
        List<Comment> children = commentRepository.findByParentCommentIdOrderByCommentIdDesc(parentId);

        // 2. DTO 리스트로 변환하여 반환
        return children.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    public CommentResponse updateComment(Long commentId, CommentRequest request, String email){
        // 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));

        // 본인인지 확인
        if(!comment.getUser().getEmail().equals(email)){
            throw new BusinessLogicException(ExceptionCode.NOT_AUTHORIZED);
        }

        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        return new CommentResponse(comment);
    }

    public void removeComment(Long commentId, String email){
        // 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));

        // 본인인지 확인
        if(!comment.getUser().getEmail().equals(email)){
            throw new BusinessLogicException(ExceptionCode.NOT_AUTHORIZED);
        }

        commentRepository.delete(comment);
    }
}
