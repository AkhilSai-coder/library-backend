package com.griet.library.controller;

import com.griet.library.dto.MemberDTO;
import com.griet.library.model.User;
import com.griet.library.repository.BorrowRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@CrossOrigin
public class MemberController {

    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;

    @GetMapping
    public List<MemberDTO> getMembers() {

        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {

            int active = borrowRepository
                    .findByUserAndReturnedFalse(user).size();

            int history = borrowRepository
                    .findByUserAndReturnedTrue(user).size();

            return new MemberDTO(
                    user.getEmail(),          // or roll number
                    user.getRole().name(),
                    active,
                    history
            );

        }).toList();
    }
}