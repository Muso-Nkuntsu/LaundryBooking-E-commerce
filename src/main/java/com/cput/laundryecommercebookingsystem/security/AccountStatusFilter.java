package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Talent Nocuze
 * 230405886
 */
public class AccountStatusFilter extends OncePerRequestFilter {

    private final IStudentRepository students;

    public AccountStatusFilter(IStudentRepository students) {
        this.students = students;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt && !isStillValid(jwt)) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,"
                    + "\"detail\":\"Your session is no longer valid. Please sign in again.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isStillValid(JwtAuthenticationToken jwt) {
        Object uid = jwt.getToken().getClaim("uid");
        if (!(uid instanceof Number number)) {
            return false;
        }
        Optional<Student> student = students.findById(number.longValue());
        if (student.isEmpty() || !student.get().isActive()) {
            return false;
        }
        List<String> roles = jwt.getToken().getClaimAsStringList("roles");
        return roles != null && roles.contains(student.get().getRole().name());
    }
}
