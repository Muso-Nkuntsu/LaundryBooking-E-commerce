package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.cput.laundryecommercebookingsystem.service.admin.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<UserRow> list() {
        return adminService.listUsers();
    }

    @GetMapping("/{id}")
    public UserRow get(@PathVariable Long id) {
        return adminService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserRow update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return adminService.updateUser(id, request);
    }

    @PutMapping("/{id}/active")
    public UserRow setActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest request) {
        return adminService.setUserActive(id, request.active());
    }

    @PutMapping("/{id}/role")
    public UserRow setRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return adminService.setUserRole(id, Role.valueOf(request.role()));
    }
}
