package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.dto.AuthResponse;
import com.cput.laundryecommercebookingsystem.dto.ForgotPasswordRequest;
import com.cput.laundryecommercebookingsystem.dto.LoginRequest;
import com.cput.laundryecommercebookingsystem.dto.MessageResponse;
import com.cput.laundryecommercebookingsystem.dto.ResendVerificationRequest;
import com.cput.laundryecommercebookingsystem.dto.ResetPasswordRequest;
import com.cput.laundryecommercebookingsystem.dto.VerifyEmailRequest;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.security.JwtService;
import com.cput.laundryecommercebookingsystem.service.IEmailVerificationService;
import com.cput.laundryecommercebookingsystem.service.IPasswordResetService;
import com.cput.laundryecommercebookingsystem.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/*StudentController.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 * Security: login returns a token; a student can only read or update their own account.
 * /getall and /delete are admin-only (see SecurityConfig).
 */

@RestController
@RequestMapping("/student")
public class StudentController {

    private final IStudentService studentService;
    private final IEmailVerificationService emailVerificationService;
    private final IPasswordResetService passwordResetService;
    private final JwtService jwtService;
    private final CurrentStudent currentStudent;

    @Autowired
    public StudentController(IStudentService studentService,
                             IEmailVerificationService emailVerificationService,
                             IPasswordResetService passwordResetService,
                             JwtService jwtService,
                             CurrentStudent currentStudent) {
        this.studentService = studentService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
        this.jwtService = jwtService;
        this.currentStudent = currentStudent;
    }

    @PostMapping("/create")
    public ResponseEntity<Student> create(@RequestBody Student student) {
        // Invalid details give 400 and a duplicate email gives 409 (see GlobalExceptionHandler).
        return ResponseEntity.ok(studentService.createStudent(student));
    }

    /** Returns { token, expiresInMs, student }. The frontend sends the token on every later request. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Student> student = studentService.login(request.email(), request.password());

        if (student.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The email or password is incorrect.");
        }

        Student s = student.get();
        return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(s), jwtService.getExpirationMs(), s));
    }

    /** The logged-in student. */
    @GetMapping("/me")
    public ResponseEntity<Student> me() {
        return studentService.getStudentById(currentStudent.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Confirms the email address using the token from the emailed link. 400 when it is invalid or expired. */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.token());
        return ResponseEntity.ok(new MessageResponse("Your email address has been verified. You can now log in."));
    }

    /** Sends a new verification email. The reply is the same whether or not the address has an account. */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "If that email belongs to an account that still needs verifying, we've sent a new verification link."));
    }

    /** Emails a password reset link. The reply is the same whether or not the address has an account. */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "If an account exists for that email, we've sent a link to reset your password."));
    }

    /** Sets a new password using the token from the emailed link. 400 when it is invalid or expired. */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been changed. You can now log in."));
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Student> read(@PathVariable Long id) {
        currentStudent.checkOwner(id);

        Optional<Student> student = studentService.getStudentById(id);

        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    public ResponseEntity<Student> update(@RequestBody Student student) {
        currentStudent.checkOwner(student.getStudentId());
        return ResponseEntity.ok(studentService.updateStudent(student));
    }

    /** Admin only (SecurityConfig). */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {

        studentService.deleteStudent(id);
        return ResponseEntity.ok(true);
    }

    /** Admin only (SecurityConfig). */
    @GetMapping("/getall")
    public ResponseEntity<List<Student>> getAll() {

        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
