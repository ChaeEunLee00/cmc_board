package com.cmc.board.post.controller;

import com.cmc.board.category.service.CategoryService;
import com.cmc.board.post.dto.PostRequest;
import com.cmc.board.post.dto.PostResponse;
import com.cmc.board.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostPageController {

    private final PostService postService;
    private final CategoryService categoryService;

    // ✅ 화면에서 쓰기 좋은 형태로 카테고리 목록 정규화
    public record CategoryOption(Long id, String name) {}

    private List<CategoryOption> loadCategoryOptions() {
        List<?> raw = (List<?>) categoryService.findCategories(); // 반환타입 몰라도 OK
        List<CategoryOption> result = new ArrayList<>();

        if (raw == null) return result;

        for (Object c : raw) {
            Long id = tryGetLong(c, "getCategoryId");
            if (id == null) id = tryGetLong(c, "getId");

            String name = tryGetString(c, "getName");

            if (id != null && name != null) {
                result.add(new CategoryOption(id, name));
            }
        }
        return result;
    }

    private Long tryGetLong(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object v = m.invoke(target);
            if (v == null) return null;
            if (v instanceof Long l) return l;
            if (v instanceof Integer i) return i.longValue();
            if (v instanceof Number n) return n.longValue();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String tryGetString(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object v = m.invoke(target);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ✅ 목록: 카테고리 필터(이름 기반) 지원
    // GET /posts?categoryName=자유
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String categoryName,
                       Model model) {

        List<CategoryOption> categories = loadCategoryOptions();
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryName", categoryName);

        List<PostResponse> posts = postService.findPosts(page, size);

        if (categoryName != null && !categoryName.isBlank()) {
            posts = posts.stream()
                    .filter(p -> Objects.equals(categoryName, p.getCategory())) // PostResponse.category = "이름"
                    .toList();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        return "posts/list";
    }

    // ✅ 상세
    @GetMapping("/{postId}")
    public String detail(@PathVariable Long postId, Model model) {
        model.addAttribute("post", postService.findPost(postId));
        return "posts/detail";
    }

    // ✅ 작성 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postRequest", new PostRequest());
        model.addAttribute("formTitle", "게시글 작성");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("categories", loadCategoryOptions());
        return "posts/form";
    }

    // ✅ 작성 처리
    @PostMapping
    public String create(@Valid @ModelAttribute("postRequest") PostRequest request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "게시글 작성");
            model.addAttribute("formAction", "/posts");
            model.addAttribute("categories", loadCategoryOptions());
            return "posts/form";
        }

        postService.createPost(request, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/posts";
    }

    // ✅ 수정 폼
    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long postId, Model model) {
        PostResponse post = postService.findPost(postId);

        PostRequest form = new PostRequest();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());

        // ✅ PostResponse.category(이름) -> id 찾아서 select가 자동 선택되게 세팅
        List<CategoryOption> categories = loadCategoryOptions();
        Long matchedId = categories.stream()
                .filter(c -> Objects.equals(c.name(), post.getCategory()))
                .map(CategoryOption::id)
                .findFirst()
                .orElse(null);
        form.setCategory(matchedId);

        model.addAttribute("postId", postId);
        model.addAttribute("postRequest", form);
        model.addAttribute("currentCategoryName", post.getCategory());
        model.addAttribute("formTitle", "게시글 수정");
        model.addAttribute("formAction", "/posts/" + postId + "/edit");
        model.addAttribute("categories", categories);

        return "posts/form";
    }

    // ✅ 수정 처리
    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable Long postId,
                       @Valid @ModelAttribute("postRequest") PostRequest request,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            model.addAttribute("formTitle", "게시글 수정");
            model.addAttribute("formAction", "/posts/" + postId + "/edit");
            model.addAttribute("categories", loadCategoryOptions());
            return "posts/form";
        }

        postService.updatePost(postId, request, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/posts/" + postId;
    }

    // ✅ 삭제
    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {

        postService.removePost(postId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/posts";
    }
}
