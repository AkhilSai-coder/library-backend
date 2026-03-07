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

            long active = borrowRepository.countByUserAndReturnedFalse(user);
            long history = borrowRepository.countByUserAndReturnedTrue(user);

            return new MemberDTO(
                    user.getCollegeId(),
                    user.getRole().name(),
                    (int) active,
                    (int) history
            );

        }).toList();
    }

}