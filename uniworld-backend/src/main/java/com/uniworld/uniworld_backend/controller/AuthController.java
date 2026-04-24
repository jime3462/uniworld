package com.uniworld.uniworld_backend.controller;

import com.uniworld.uniworld_backend.dto.AuthRequest;
import com.uniworld.uniworld_backend.dto.AuthResponse;
import com.uniworld.uniworld_backend.dto.RegisterRequest;
import com.uniworld.uniworld_backend.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
