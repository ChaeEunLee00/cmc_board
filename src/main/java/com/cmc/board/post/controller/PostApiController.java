package com.cmc.board.post.controller;

import com.cmc.board.post.dto.PostRequest;
import com.cmc.board.post.dto.PostResponse;
import com.cmc.board.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity postPost(@Valid @RequestBody PostRequest request,
                                   @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(postService.createPost(request, userDetails.getUsername()));
    }

    // 게시글 조회
    @GetMapping("/{post-id}")
    public ResponseEntity getPost(@PathVariable("post-id") Long postId){
        return ResponseEntity.ok(postService.findPost(postId));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity getPosts(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size){
        List<PostResponse> posts = postService.findPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PatchMapping("/{post-id}")
    public ResponseEntity patchPost(@PathVariable("post-id") Long postId,
                                    @Valid @RequestBody PostRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(postService.updatePost(postId, request, userDetails.getUsername()));
    }

    // 게시글 삭제
    @DeleteMapping("/{post-id}")
    public ResponseEntity deletePost(@PathVariable("post-id") Long postId,
                                     @AuthenticationPrincipal UserDetails userDetails){
        postService.removePost(postId, userDetails.getUsername());
        return ResponseEntity.ok("게시글 삭제가 완료되었습니다.");
    }
}
