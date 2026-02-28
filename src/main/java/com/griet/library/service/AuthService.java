package com.griet.library.service;

import com.griet.library.model.Role;
import com.griet.library.model.User;
import com.griet.library.repository.UserRepository;
import com.griet.library.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ================= REGISTER =================
    public String register(String name, String email, String password, Role role) {

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);

        // ✅ FIXED HERE
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ================= LOGIN =================
    public String login(String email, String password, Role role) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.getRole().equals(role)) {
            throw new RuntimeException("Invalid role selected");
        }

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }
}