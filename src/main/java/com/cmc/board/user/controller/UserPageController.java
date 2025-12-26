package com.cmc.board.user.controller;

import com.cmc.board.user.dto.UserRequest;
import com.cmc.board.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // ✅ HTML(Thymeleaf) 페이지를 리턴하는 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/users") // ✅ 화면 URL: /users/...
public class UserPageController {

    private final UserService userService;

    // 1) 회원가입 페이지 보여주기 (GET /users/signup)
    @GetMapping("/signup")
    public String signupForm(Model model) {
        // ✅ form이 바인딩될 객체를 모델에 담아둔다 (th:object="${userRequest}" 와 연결)
        model.addAttribute("userRequest", new UserRequest());
        return "users/signup"; // ✅ templates/users/signup.html
    }

    // 2) 회원가입 처리 (POST /users/signup)
    @PostMapping("/signup")
    public String signupSubmit(
            @Valid @ModelAttribute("userRequest") UserRequest request, // ✅ 폼 입력값 -> UserRequest에 자동 매핑
            BindingResult bindingResult,                               // ✅ @Valid 검증 실패 시 에러가 여기에 담김
            RedirectAttributes redirectAttributes
    ) {
        // ✅ 검증 에러가 있으면 다시 signup 페이지로 돌아가서 에러 표시
        if (bindingResult.hasErrors()) {
            return "users/signup";
        }

        // ✅ 실제 회원 생성 로직
        userService.createUser(request);

        // ✅ 성공 메시지를 리다이렉트 후에도 보이게 flash로 전달
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");

        // ✅ 보통 회원가입 후 로그인으로 보내는 흐름
        return "redirect:/login";
    }

    // 3) 회원탈퇴 확인 페이지 (GET /users/delete)
    @GetMapping("/delete")
    public String deleteConfirm() {
        return "users/delete"; // ✅ templates/users/delete.html
    }

    // 4) 회원탈퇴 처리 (POST /users/delete)
    @PostMapping("/delete")
    public String deleteUser(
            @AuthenticationPrincipal UserDetails userDetails, // ✅ 로그인한 사용자
            HttpServletRequest request,                        // ✅ logout 호출용
            RedirectAttributes redirectAttributes
    ) throws Exception {
        // ✅ API 컨트롤러의 delete 로직과 동일
        userService.deleteUser(userDetails.getUsername());

        // ✅ 세션 로그아웃
        request.logout();

        redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
        return "redirect:/";
    }
}
