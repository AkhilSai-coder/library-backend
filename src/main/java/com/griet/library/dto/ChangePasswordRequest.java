package com.griet.library.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String collegeId;
    private String oldPassword;
    private String newPassword;
}
