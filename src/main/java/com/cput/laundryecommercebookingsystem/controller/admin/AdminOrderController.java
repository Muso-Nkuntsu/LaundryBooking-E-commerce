package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*AdminOrderController.java
 * Admin only: shop orders and payments (including checking EFTs).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService orderService;

    public AdminOrderController(AdminOrderService orderService) {
        this.orderService = orderService;
    }

    // ---------- Orders ----------

    /** Example: GET /api/admin/orders?status=OUTSTANDING&q=nontshu  (OUTSTANDING = paid but not yet completed) */
    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderView>> orders(@RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String q) {
        return ResponseEntity.ok(orderService.listOrders(status, q));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderDetail> order(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    /** Body: { "status": "IN_PROGRESS" }. Cancelling puts the stock back. */
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<AdminOrderDetail> updateOrderStatus(@PathVariable Long id,
                                                              @RequestBody OrderStatusRequest request) {
        OrderStatus status = request == null ? null : request.status();
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // ---------- Payments ----------

    /** Example: GET /api/admin/payments?status=PENDING&method=EFT */
    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentView>> payments(@RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String method,
                                                           @RequestParam(required = false) String q) {
        return ResponseEntity.ok(orderService.listPayments(status, method, q));
    }

    /** Body: { "approved": true } when the EFT is in the bank; false when it isn't. */
    @PatchMapping("/payments/{id}/verify")
    public ResponseEntity<AdminPaymentView> verifyPayment(@PathVariable Long id,
                                                          @RequestBody VerifyPaymentRequest request) {
        if (request == null || request.approved() == null) {
            throw new IllegalArgumentException("Send approved: true or false");
        }
        return ResponseEntity.ok(orderService.verifyPayment(id, request.approved()));
    }
}
