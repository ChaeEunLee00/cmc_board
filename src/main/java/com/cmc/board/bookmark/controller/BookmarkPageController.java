package com.cmc.board.bookmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BookmarkPageController {

    // ✅ 북마크 목록 화면
    // GET /bookmarks
    @GetMapping("/bookmarks")
    public String bookmarksPage() {
        return "bookmarks/list"; // templates/bookmarks/list.html
    }
}
