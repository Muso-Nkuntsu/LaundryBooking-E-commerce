package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.repository.PaymentRepository;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.service.IStudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/*AdminUserService.java
 * Admin dashboard: view students, edit their details, activate or deactivate accounts,
 * give or remove admin access, and confirm an email address by hand.
 * Safety rails: an admin can't deactivate or demote themselves, and the last active admin can't be removed.
 */
@Service
public class AdminUserService {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String PHONE_PATTERN = "^[0-9+ ]{7,20}$";

    private final IStudentRepository studentRepository;
    private final IStudentService studentService;
    private final iBookingRepository bookingRepository;
    private final IOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentStudent currentStudent;

    public AdminUserService(IStudentRepository studentRepository, IStudentService studentService,
                            iBookingRepository bookingRepository, IOrderRepository orderRepository,
                            PaymentRepository paymentRepository, CurrentStudent currentStudent) {
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.currentStudent = currentStudent;
    }

    /**
     * @param q      matches name, email or phone (optional)
     * @param role   USER or ADMIN (optional)
     * @param status "active", "inactive" or "unverified" (optional)
     */
    @Transactional(readOnly = true)
    public List<AdminUserView> list(String q, Role role, String status) {
        String search = q == null ? "" : q.trim().toLowerCase();
        return studentRepository.findAll().stream()
                .filter(s -> search.isEmpty()
                        || contains(s.getFirstName() + " " + s.getLastName(), search)
                        || contains(s.getEmail(), search)
                        || contains(s.getPhoneNumber(), search))
                .filter(s -> role == null || s.getRole() == role)
                .filter(s -> status == null || status.isBlank()
                        || ("active".equalsIgnoreCase(status) && !s.isDisabled())
                        || ("inactive".equalsIgnoreCase(status) && s.isDisabled())
                        || ("unverified".equalsIgnoreCase(status) && !s.isEmailVerified()))
                .sorted(Comparator.comparing(Student::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(AdminUserView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserDetail detail(Long id) {
        Student student = find(id);
        List<Booking> bookings = bookingRepository.findByStudent(student);
        List<Order> orders = orderRepository.findByStudentPrimaryKey(id);

        Set<Long> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toSet());
        Set<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toSet());
        double totalPaid = paymentRepository.findAll().stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .filter(p -> (p.getBookingId() != null && bookingIds.contains(p.getBookingId()))
                        || (p.getOrderId() != null && orderIds.contains(p.getOrderId())))
                .mapToDouble(Payment::getAmount)
                .sum();

        List<AdminBookingView> recentBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .limit(5)
                .map(AdminBookingView::from)
                .toList();
        List<AdminOrderView> recentOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderId).reversed())
                .limit(5)
                .map(o -> new AdminOrderView(o.getOrderId(), id, student.getFirstName() + " " + student.getLastName(),
                        student.getEmail(), o.getOrderDate(), o.getTotalAmount(), o.getStatus().name(), 0, ""))
                .toList();

        return new AdminUserDetail(AdminUserView.from(student), bookings.size(), orders.size(),
                Math.round(totalPaid * 100) / 100.0, recentBookings, recentOrders);
    }

    /** Changing the email means the student must verify the new address (handled by StudentServiceImpl). */
    @Transactional
    public AdminUserView update(Long id, UpdateUserRequest req) {
        find(id);
        if (req == null || isBlank(req.firstName()) || isBlank(req.lastName())) {
            throw new IllegalArgumentException("First name and last name are required");
        }
        if (isBlank(req.email()) || !req.email().trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (isBlank(req.phoneNumber()) || !req.phoneNumber().trim().matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid phone number (digits only)");
        }
        // The password is left empty, so StudentServiceImpl keeps the existing one.
        Student changes = new Student.Builder()
                .setStudentId(id)
                .setFirstName(req.firstName().trim())
                .setLastName(req.lastName().trim())
                .setEmail(req.email().trim())
                .setPhoneNumber(req.phoneNumber().trim())
                .build();
        return AdminUserView.from(studentService.updateStudent(changes));
    }

    /** A deactivated student is locked out straight away (JwtAuthFilter checks on every request). */
    @Transactional
    public AdminUserView setEnabled(Long id, boolean enabled) {
        Student student = find(id);
        if (!enabled && id.equals(currentStudent.id())) {
            throw new IllegalArgumentException("You can't deactivate your own account");
        }
        if (!enabled && student.getRole() == Role.ADMIN && !student.isDisabled() && isLastActiveAdmin()) {
            throw new IllegalStateException("You can't deactivate the last active administrator");
        }
        student.setDisabled(!enabled);
        return AdminUserView.from(studentRepository.save(student));
    }

    @Transactional
    public AdminUserView setRole(Long id, Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Choose USER or ADMIN");
        }
        Student student = find(id);
        if (id.equals(currentStudent.id())) {
            throw new IllegalArgumentException("You can't change your own role");
        }
        if (student.getRole() == Role.ADMIN && role == Role.USER && !student.isDisabled() && isLastActiveAdmin()) {
            throw new IllegalStateException("You can't remove the last active administrator");
        }
        student.changeRole(role);
        return AdminUserView.from(studentRepository.save(student));
    }

    /** For when the verification email didn't arrive (for example, no mail server is set up yet). */
    @Transactional
    public AdminUserView markEmailVerified(Long id) {
        Student student = find(id);
        if (!student.isEmailVerified()) {
            student.markEmailVerified(LocalDateTime.now());
            student = studentRepository.save(student);
        }
        return AdminUserView.from(student);
    }

    private boolean isLastActiveAdmin() {
        return studentRepository.findAll().stream()
                .filter(s -> s.getRole() == Role.ADMIN && !s.isDisabled())
                .count() <= 1;
    }

    private Student find(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + id));
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
