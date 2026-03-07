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

    // REGISTER
    public String register(String name, String collegeId, String password, Role role) {

        if (userRepository.existsByCollegeId(collegeId)) {
            throw new RuntimeException("User already exists");
        }

        User user = User.builder()
                .name(name)
                .collegeId(collegeId)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);

        return jwtService.generateToken(user.getCollegeId(), user.getRole().name());
    }

    // LOGIN
    public String login(String collegeId, String password, Role role) {

        User user = userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getRole().equals(role)) {
            throw new RuntimeException("Invalid role");
        }

        return jwtService.generateToken(user.getCollegeId(), user.getRole().name());
    }


}
