package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public Overview overview() {
        return adminService.overview();
    }

    @GetMapping("/reports")
    public Reports reports() {
        return adminService.reports();
    }
}
