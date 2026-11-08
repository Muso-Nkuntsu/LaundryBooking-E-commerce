package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.DashboardView;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.ReportView;
import com.cput.laundryecommercebookingsystem.service.admin.AdminReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*AdminDashboardController.java
 * Admin only: protected twice, by the /api/admin/** rule in SecurityConfig and by @PreAuthorize here.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminReportService reportService;

    public AdminDashboardController(AdminReportService reportService) {
        this.reportService = reportService;
    }

    /** The numbers and recent activity on the admin home page. */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardView> dashboard() {
        return ResponseEntity.ok(reportService.dashboard());
    }

    /** Booking, order, payment and stock statistics for the last N days (default 30). */
    @GetMapping("/reports")
    public ResponseEntity<ReportView> reports(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.report(days));
    }
}
