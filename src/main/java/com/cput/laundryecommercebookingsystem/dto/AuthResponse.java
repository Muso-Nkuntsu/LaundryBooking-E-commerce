package com.cput.laundryecommercebookingsystem.dto;

import com.cput.laundryecommercebookingsystem.domain.Student;

/** What /student/login returns: the token to send on every request, and the student. */
public record AuthResponse(String token, long expiresInMs, Student student) {
}
