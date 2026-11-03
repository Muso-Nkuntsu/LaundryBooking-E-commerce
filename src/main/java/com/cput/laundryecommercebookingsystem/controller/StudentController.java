package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.dto.LoginRequest;
import com.cput.laundryecommercebookingsystem.dto.MessageResponse;
import com.cput.laundryecommercebookingsystem.dto.ResendVerificationRequest;
import com.cput.laundryecommercebookingsystem.dto.VerifyEmailRequest;
import com.cput.laundryecommercebookingsystem.service.IEmailVerificationService;
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
 */

@RestController
@RequestMapping("/student")
public class StudentController {

    private final IStudentService studentService;
    private final IEmailVerificationService emailVerificationService;

    @Autowired
    public StudentController(IStudentService studentService,
                             IEmailVerificationService emailVerificationService) {
        this.studentService = studentService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/create")
    public ResponseEntity<Student> create(@RequestBody Student student) {
        // Invalid details give 400 and a duplicate email gives 409 (see GlobalExceptionHandler).
        return ResponseEntity.ok(studentService.createStudent(student));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Student> student = studentService.login(request.email(), request.password());

        if (student.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The email or password is incorrect.");
        }

        return ResponseEntity.ok(student.get());
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

    @GetMapping("/read/{id}")
    public ResponseEntity<Student> read(@PathVariable Long id) {

        Optional<Student> student = studentService.getStudentById(id);

        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    public ResponseEntity<Student> update(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.updateStudent(student));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {

        studentService.deleteStudent(id);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Student>> getAll() {

        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
