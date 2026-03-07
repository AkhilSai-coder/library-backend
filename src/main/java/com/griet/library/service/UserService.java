package com.griet.library.service;

import com.griet.library.model.User;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

// ==============================
// GET USER PROFILE
// ==============================

    public User getUserByCollegeId(String collegeId) {

        return userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

// ==============================
// CHANGE PASSWORD
// ==============================

    public String changePassword(String collegeId, String oldPassword, String newPassword) {

        User user = userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        // encode new password
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return "Password updated successfully";
    }

}
