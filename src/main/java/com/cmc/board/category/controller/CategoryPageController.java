package com.cmc.board.category.controller;

import com.cmc.board.category.dto.CategoryRequest;
import com.cmc.board.category.service.CategoryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories") // ✅ 화면 URL
public class CategoryPageController {

    private final CategoryService categoryService;

    // ✅ 카테고리 관리 페이지
    @GetMapping
    public String page(Model model) {
        model.addAttribute("categories", categoryService.findCategories());
        return "categories/list";
    }

    // ✅ 카테고리 생성 (CategoryRequest는 NoArgsConstructor가 없으니 @RequestParam으로 받음)
    @PostMapping
    public String create(@RequestParam("name") @NotBlank String name,
                         RedirectAttributes redirectAttributes) {

        categoryService.createCategory(new CategoryRequest(name));
        redirectAttributes.addFlashAttribute("message", "카테고리가 생성되었습니다.");
        return "redirect:/categories";
    }

    // ✅ 카테고리 삭제 (HTML 폼용)
    @PostMapping("/{categoryId}/delete")
    public String delete(@PathVariable Long categoryId,
                         RedirectAttributes redirectAttributes) {

        categoryService.removeCategory(categoryId);
        redirectAttributes.addFlashAttribute("message", "카테고리가 삭제되었습니다.");
        return "redirect:/categories";
    }
}
