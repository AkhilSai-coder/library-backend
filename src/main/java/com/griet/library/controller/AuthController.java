package com.griet.library.controller;

import com.griet.library.dto.LoginRequest;
import com.griet.library.repository.UserRepository;
import com.griet.library.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")   // 🔥 Important: Base URL = /auth
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // ✅ LOGIN ENDPOINT
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        // 1️⃣ Find user by email
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 2️⃣ Validate password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        // 3️⃣ Generate JWT
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()   // pass enum name
        );
    }
}