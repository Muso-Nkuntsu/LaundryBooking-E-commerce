package com.cput.laundryecommercebookingsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/*RateLimitFilter.java
 * At most 5 attempts per minute from one IP address on the endpoints people try to guess or abuse.
 * Kept in memory, which is enough for one server. Created in SecurityConfig (not a @Component).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000;
    private static final Set<String> LIMITED_PATHS = Set.of(
            "/student/login",
            "/student/create",
            "/student/forgot-password",
            "/student/reset-password",
            "/student/resend-verification",
            "/payment/create");

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = request.getRemoteAddr() + "|" + request.getRequestURI();
        long now = System.currentTimeMillis();
        Deque<Long> times = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() > WINDOW_MS) {
                times.pollFirst();
            }
            if (times.size() >= MAX_REQUESTS) {
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Too many attempts. Please wait a minute and try again.");
                return;
            }
            times.addLast(now);
        }
        chain.doFilter(request, response);
    }
}
