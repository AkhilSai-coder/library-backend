package com.griet.library.controller;

import com.griet.library.dto.ChangePasswordRequest;
import com.griet.library.model.User;
import com.griet.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET USER PROFILE
    @GetMapping("/profile/{collegeId}")
    public User getProfile(@PathVariable String collegeId){
        return userService.getUserByCollegeId(collegeId);
    }

    // CHANGE PASSWORD
    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestBody ChangePasswordRequest request){

        return userService.changePassword(
                authentication.getName(),
                request.getOldPassword(),
                request.getNewPassword()
        );
    }
}