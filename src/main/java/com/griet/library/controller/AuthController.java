package com.griet.library.controller;

import com.griet.library.dto.LoginRequest;
import com.griet.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://charming-cajeta-c2f8f5.netlify.app")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // REGISTER
    @PostMapping("/registermembers")
    public String register(@RequestBody LoginRequest request) {
        return authService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
    }

    // LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
    }
}