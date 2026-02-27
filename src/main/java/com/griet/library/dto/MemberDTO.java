package com.griet.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {

    private String userId;
    private String role;
    private int activeCount;
    private int historyCount;
}