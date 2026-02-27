package com.griet.library.controller;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}