package org.example.hospital.auth;

import lombok.RequiredArgsConstructor;
import org.example.hospital.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public Map<String, String> login(@RequestParam(value = "logout", required = false) String logout) {
        Map<String, String> response = new HashMap<>();
        if (logout != null) {
            response.put("message", "You have been logged out successfully.");
        } else {
            response.put("message", "Please provide credentials to log in.");
        }
        return response;
    }

    @GetMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully.");
        return response;
    }

    @RequestMapping("/error")
    public Map<String, String> handleError() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "An error occurred. Please try again.");
        return response;
    }
}