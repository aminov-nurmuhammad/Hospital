package org.example.hospital.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.hospital.repo.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class Login {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/auth/login?logout";
    }


    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        return "redirect:/auth/login";
    }
}