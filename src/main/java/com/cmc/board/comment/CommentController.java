package com.cmc.board.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/comments")
    public ResponseEntity postComment(@Valid @RequestBody CommentRequest request,
                                      @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(commentService.createComment(request, userDetails.getUsername()));
    }

    // 특정 게시글의 댓글 목록 조회
    @GetMapping("/posts/{post-id}/comments")
    public ResponseEntity getPostComments(@PathVariable("post-id") Long postId){
        return ResponseEntity.ok(commentService.findPostComments(postId));
    }

    // 특정 댓글의 대댓글 목록 조회
    @GetMapping("comments/{comment-id}")
    public ResponseEntity getCommentComments(@PathVariable("comment-id") Long commentId){
        return ResponseEntity.ok(commentService.findChildComments(commentId));
    }

    // 댓글 수정
    @PatchMapping("/comments/{comment-id}")
    public ResponseEntity patchComment(@PathVariable("comment-id") Long commentId,
                                      @Valid @RequestBody CommentRequest request,
                                      @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(commentService.updateComment(commentId, request, userDetails.getUsername()));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{comment-id}")
    public ResponseEntity deleteComment(@PathVariable("comment-id") Long commentId,
                                      @AuthenticationPrincipal UserDetails userDetails){
        commentService.removeComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}
