package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.dto.LoginRequest;
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

    @Autowired
    public StudentController(IStudentService studentService) {
        this.studentService = studentService;
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
