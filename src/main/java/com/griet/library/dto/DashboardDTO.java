package com.griet.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardDTO {

    private long totalBooks;
    private long totalUsers;
    private long borrowedBooks;
    private long overdueBooks;

}
