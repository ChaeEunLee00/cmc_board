package com.cmc.board.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 북마크 생성
    @PostMapping("/posts/{post-id}/bookmarks")
    public ResponseEntity postBookmark(@PathVariable("post-id") Long postId,
                                       @AuthenticationPrincipal UserDetails userDetails){
        bookmarkService.createBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok("북마크가 생성되었습니다.");
    }

    // 북마크한 게시글 목록 조회
    @GetMapping("/bookmarks")
    public ResponseEntity getBookmarks(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(bookmarkService.findBookmarks(userDetails.getUsername()));
    }

    // 북마크 제거
    @DeleteMapping("/posts/{post-id}/bookmarks")
    public ResponseEntity deleteBookmark(@PathVariable("post-id") Long postId,
                                         @AuthenticationPrincipal UserDetails userDetails){
        bookmarkService.removeBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok("북마크가 제거되었습니다.");
    }


}
