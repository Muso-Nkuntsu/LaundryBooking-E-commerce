package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*JwtAuthFilter.java
 * Reads "Authorization: Bearer <token>" and, when the token is valid, marks the request as that student.
 * The student is loaded from the database on every request, so a deactivated account or a changed
 * role takes effect straight away. Created in SecurityConfig (not a @Component) so it only runs once.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final IStudentRepository studentRepository;

    public JwtAuthFilter(JwtService jwtService, IStudentRepository studentRepository) {
        this.jwtService = jwtService;
        this.studentRepository = studentRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.parseStudentId(header.substring(7).trim())
                    .flatMap(studentRepository::findById)
                    .filter(student -> !student.isDisabled() && student.isEmailVerified())
                    .ifPresent(student -> {
                        AuthenticatedStudent principal = new AuthenticatedStudent(
                                student.getStudentId(), student.getEmail(), student.getRole());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + student.getRole().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
