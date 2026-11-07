package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*CurrentStudent.java
 * The only trusted answer to "who is calling?". A studentId sent by the browser is never
 * trusted on its own: controllers check it here first.
 */
@Component
public class CurrentStudent {

    public AuthenticatedStudent get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedStudent student)) {
            throw new AuthenticationCredentialsNotFoundException("Please log in.");
        }
        return student;
    }

    public Long id() {
        return get().studentId();
    }

    public boolean isAdmin() {
        return get().role() == Role.ADMIN;
    }

    /** Students may only use their own records; an admin may use anyone's. Otherwise 403. */
    public void checkOwner(Long ownerStudentId) {
        if (isAdmin()) {
            return;
        }
        if (ownerStudentId == null || !ownerStudentId.equals(id())) {
            throw new AccessDeniedException("You can only access your own records.");
        }
    }

    public void checkOwner(Student owner) {
        checkOwner(owner == null ? null : owner.getStudentId());
    }

    /** For requests that carry a studentId: a student always acts as themselves; an admin may choose. */
    public Long resolveStudentId(Long requestedStudentId) {
        if (requestedStudentId != null && isAdmin()) {
            return requestedStudentId;
        }
        return id();
    }
}
