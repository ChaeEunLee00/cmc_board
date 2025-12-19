package com.cmc.board.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
    public ResponseEntity postCategory(@Valid @RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    // 카테고리 목록 조회
    @GetMapping
    public ResponseEntity getCategories(){
        return ResponseEntity.ok(categoryService.findCategories());
    }

    // 카테고리 삭제
    @DeleteMapping("{category-id}")
    public ResponseEntity deleteCategories(@PathVariable("category-id") Long categoryId){
        categoryService.removeCategory(categoryId);
        return ResponseEntity.ok("카테고리가 삭제되었습니다.");
    }
}
