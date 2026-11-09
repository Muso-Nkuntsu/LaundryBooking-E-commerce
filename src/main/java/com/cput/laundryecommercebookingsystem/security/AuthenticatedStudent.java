package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.domain.enums.Role;

/** Who is making the current request, taken from their checked login token. */
public record AuthenticatedStudent(Long studentId, String email, Role role) {
}
