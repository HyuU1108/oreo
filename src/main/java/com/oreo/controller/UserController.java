package com.oreo.controller;

import com.oreo.entity.User;
import com.oreo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {
        if (userService.register(user, model)) {
            return "redirect:/login";
        } else {
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(value = "email") String email, 
            @RequestParam(value = "password") String password,
            HttpSession session,
            Model model) {
        User user = userService.login(email, password);
        if (user != null) {
            session.setAttribute("loginUser", user);
            return "redirect:/";
        } else {
            model.addAttribute("error", "이메일 또는 비밀번호가 잘못되었습니다.");
            return "user/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/session-test")
    public String sessionTest(HttpSession session,
            HttpServletRequest request,
            Model model) {

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("nickname", ((User) loginUser).getNickname());
        }
        model.addAttribute("sessionId", session.getId());
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Map<String, String> cookieMap = new HashMap<>();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
            model.addAttribute("cookies", cookieMap);
        }
        return "user/session-test";
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loginUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/update-nickname")
    public String updateNickname(@RequestParam(value = "nickname") String nickname,
            HttpSession session,
            Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/login";

        try {
            userService.updateNickname(loginUser.getId(), nickname);
            loginUser.setNickname(nickname);
            model.addAttribute("message", "닉네임이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("user", loginUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/update-password")
    public String updatePassword(@RequestParam(value = "currentPassword") String currentPassword,
            @RequestParam(value = "newPassword") String newPassword,
            HttpSession session,
            Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        boolean success = userService.updatePassword(loginUser.getId(), currentPassword, newPassword);
        model.addAttribute("user", loginUser);
        if (success) {
            model.addAttribute("message", "비밀번호가 변경되었습니다.");
        } else {
            model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
        }
        return "user/mypage";
    }
}