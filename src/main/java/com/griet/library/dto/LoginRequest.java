package com.griet.library.dto;

import com.griet.library.model.Role;
import lombok.Data;

@Data
public class LoginRequest {

    private String name;
    private String collegeId;
    private String password;
    private Role role;

}
