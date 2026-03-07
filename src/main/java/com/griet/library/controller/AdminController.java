package com.griet.library.controller;

import com.griet.library.dto.DashboardDTO;
import com.griet.library.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/total-books")
    public long totalBooks(){
        return adminService.totalBooks();
    }

    @GetMapping("/total-users")
    public long totalUsers(){
        return adminService.totalUsers();
    }

    @GetMapping("/borrowed-books")
    public long borrowedBooks(){
        return adminService.borrowedBooks();
    }

    @GetMapping("/overdue-books")
    public long overdueBooks(){
        return adminService.overdueBooks();
    }

    @GetMapping("/dashboard")
    public DashboardDTO dashboard(){
        return new DashboardDTO(
                adminService.totalBooks(),
                adminService.totalUsers(),
                adminService.borrowedBooks(),
                adminService.overdueBooks()
        );
    }
}