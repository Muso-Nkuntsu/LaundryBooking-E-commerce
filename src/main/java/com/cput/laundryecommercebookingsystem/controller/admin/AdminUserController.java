package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*AdminUserController.java
 * Admin only: students and administrators.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService userService;

    public AdminUserController(AdminUserService userService) {
        this.userService = userService;
    }

    /** Example: GET /api/admin/users?q=nontshu&role=ADMIN&status=active */
    @GetMapping
    public ResponseEntity<List<AdminUserView>> list(@RequestParam(required = false) String q,
                                                    @RequestParam(required = false) Role role,
                                                    @RequestParam(required = false) String status) {
        return ResponseEntity.ok(userService.list(q, role, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetail> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.detail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserView> update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /** Body: { "enabled": false } deactivates the account; true activates it again. */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<AdminUserView> setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request) {
        if (request == null || request.enabled() == null) {
            throw new IllegalArgumentException("Send enabled: true or false");
        }
        return ResponseEntity.ok(userService.setEnabled(id, request.enabled()));
    }

    /** Body: { "role": "ADMIN" } or { "role": "USER" } */
    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserView> setRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        return ResponseEntity.ok(userService.setRole(id, request == null ? null : request.role()));
    }

    /** Confirms the email address by hand, for when the verification email didn't arrive. */
    @PatchMapping("/{id}/verify-email")
    public ResponseEntity<AdminUserView> verifyEmail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.markEmailVerified(id));
    }
}
