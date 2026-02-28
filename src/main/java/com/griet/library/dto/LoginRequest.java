package com.griet.library.dto;

import com.griet.library.model.Role;
import lombok.Data;

@Data
public class LoginRequest {

    private String name;     // needed for register
    private String email;
    private String password;
    private Role role;       // VERY IMPORTANT
}