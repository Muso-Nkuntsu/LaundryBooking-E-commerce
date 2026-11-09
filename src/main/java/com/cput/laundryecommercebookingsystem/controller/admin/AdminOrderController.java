package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminService adminService;

    public AdminOrderController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/orders")
    public List<OrderRow> orders(@RequestParam(required = false) String status) {
        return adminService.listOrders(status);
    }

    @GetMapping("/orders/{id}")
    public OrderDetail order(@PathVariable Long id) {
        return adminService.getOrder(id);
    }

    @PutMapping("/orders/{id}/status")
    public OrderRow updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return adminService.updateOrderStatus(id, request.status());
    }

    @GetMapping("/payments")
    public List<PaymentRow> payments(@RequestParam(required = false) String status) {
        return adminService.listPayments(status);
    }

    /** Verify a pending payment (e.g. an EFT that has reached the bank account). */
    @PutMapping("/payments/{id}/verify")
    public PaymentRow verify(@PathVariable Long id) {
        return adminService.verifyPayment(id);
    }

    @PutMapping("/payments/{id}/reject")
    public PaymentRow reject(@PathVariable Long id) {
        return adminService.rejectPayment(id);
    }
}
